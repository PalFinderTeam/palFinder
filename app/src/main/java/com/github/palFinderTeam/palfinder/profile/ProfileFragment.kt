package com.github.palFinderTeam.palfinder.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetupListRootAdapter
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpView
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.image.QRCode
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.launch
import java.util.stream.IntStream.range


/**
 * When creating the profile page, the username (which is unique) will
 * be sent to from the previous page as an intent. A database query will be made
 * and the user info will be sent back
 */
class ProfileFragment : Fragment(R.layout.activity_profile) {

    private lateinit var meetupList: RecyclerView
    private lateinit var adapter: MeetupListRootAdapter
    private lateinit var rootView: View
    private lateinit var overflow : TextView


    private val viewModel: ProfileViewModel by activityViewModels()
    private val args: ProfileFragmentArgs by navArgs()

    companion object{
        const val EMPTY_FIELD = ""
        const val MAX_SHORT_BIO_DISPLAY_LINES = 2
        const val PROFILE_ID_ARG = "UserId"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootView = view

        val userId = args.userId
        viewModel.fetchProfile(userId)
        viewModel.fetchLoggedProfile()

        // Fetch last Meetups list through adapter
        meetupList = view.findViewById(R.id.meetup_list_recycler)
        meetupList.layoutManager = LinearLayoutManager(requireContext())

        viewModel.fetchUserMeetups(userId)

        // Bind the adapter to the RecyclerView
        viewModel.meetupDataSet.observe(viewLifecycleOwner) { dataResp ->
            if (dataResp is Response.Success) {
                val meetups = dataResp.data
                adapter = MeetupListRootAdapter(
                    meetups,
                    meetups.toMutableList(),
                    context = requireContext()
                ) { onListItemClick(it) }
                meetupList.adapter = adapter
            }
        }

        view.findViewById<ImageView>(R.id.show_qr_button).setOnClickListener{ showQR() }
        overflow = view.findViewById(R.id.userProfileDescOverflow)
        overflow.setOnClickListener { showFullDesc() }

        viewModel.profile.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Success -> {
                    injectUserInfo(it.data)
                    bindFollow(it.data)
                    bindBadgesAndAchievements(it.data)
                }
                is Response.Failure -> printToast(it.errorMessage)
            }
        }
    }

    /**
     * binds the follow/unfollow button
     */
    private fun bindFollow(profileViewed: ProfileUser) {
        val followButton = rootView.findViewById<Button>(R.id.button_follow_profile)
        val blockButton = rootView.findViewById<Button>(R.id.blackList)
        viewModel.logged_profile.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Success -> {
                   followAndBlockSystem(it.data, profileViewed, followButton, blockButton)
                }
                is Response.Failure -> printToast(it.errorMessage)
            }
        }

    }

    private fun followAndBlockSystem(loggedProfile: ProfileUser, profileViewed: ProfileUser, followButton: Button, blockButton: Button) {
        when {
            viewModel.profileService.getLoggedInUserID() == null -> {
                followButton.isEnabled = true
                blockButton.isEnabled = true
            }
            profileViewed.uuid == viewModel.profileService.getLoggedInUserID() -> {
                followButton.isEnabled = false
                blockButton.isEnabled = false
            }
            loggedProfile.canFollow(profileViewed.uuid) -> {
                followButton.text = getString(R.string.follow)
            }
            loggedProfile.canUnFollow(profileViewed.uuid) -> {
                followButton.text = getString(R.string.unfollow)
            }
        }
        if (loggedProfile.canBlock(profileViewed.uuid)) {
            blockButton.text = getString(R.string.block_user)
        } else {
            blockButton.text = getString(R.string.unblock_user)
        }
        followButton.setOnClickListener {
            if (followButton.text.equals(getString(R.string.follow))) {
                viewModel.follow(loggedProfile.uuid, profileViewed.uuid)
                followButton.text = getString(R.string.unfollow)
            } else {
                viewModel.unFollow(loggedProfile.uuid, profileViewed.uuid)
                followButton.text = getString(R.string.follow)
            }
        }
        blockButton.setOnClickListener {
            if (blockButton.text.equals(getString(R.string.block_user))) {
                viewModel.block(loggedProfile.uuid, profileViewed.uuid)
                blockButton.text = getString(R.string.unblock_user)
            } else {
                viewModel.unBlock(loggedProfile.uuid, profileViewed.uuid)
                blockButton.text = getString(R.string.block_user)
            }
        }
    }

    /**
     * Injects all user info into the profile activity
     * in the top part of the screen
     *
     * @param user: ProfileUser
     */
    private fun injectUserInfo(user: ProfileUser) {
        rootView.findViewById<TextView>(R.id.userProfileUsername).text = user.atUsername()
        rootView.findViewById<TextView>(R.id.userProfileJoinDate).apply { text = user.prettyJoinTime() }

        rootView.findViewById<TextView>(R.id.followers).text = String.format(
            getString(R.string.following_nb),
            user.followed.size
        )
        rootView.findViewById<TextView>(R.id.following).text = String.format(
            getString(R.string.followers_nb),
            user.following.size
        )
        if(user.canProfileBeSeenBy(viewModel.profileService.getLoggedInUserID()!!)) {
            rootView.findViewById<TextView>(R.id.userProfileName).text = user.fullName()
            injectBio(user.description)
        }else{
            rootView.findViewById<TextView>(R.id.userProfileName).text = this.resources.getString(R.string.private_name)
            injectBio(this.resources.getString(R.string.private_desc))
        }

        lifecycleScope.launch {
            user.pfp.loadImageInto(rootView.findViewById(R.id.userProfileImage), requireContext())
        }
    }


    /**
     * Injects the bio by applying the Read More feature
     *
     * @param bio: String
     */
    private fun injectBio(bio: String) {
        val desc = rootView.findViewById<TextView>(R.id.userProfileDescription)
        if (bio == EMPTY_FIELD) {
            rootView.findViewById<TextView>(R.id.userProfileAboutTitle).text = this.resources.getString(
                R.string.no_desc
            )
            desc.text = EMPTY_FIELD
            showFullDesc()
        } else {
            desc.text = bio
            desc.post {
                val lineCount: Int = desc.lineCount
                if (lineCount < MAX_SHORT_BIO_DISPLAY_LINES) {
                    showFullDesc()
                }
            }
        }
    }

    /**
     * Clicking on Read More will reveal the entire text
     */
    private fun showFullDesc() {
        overflow.visibility = GONE
        val desc = rootView.findViewById<TextView>(R.id.userProfileDescription)
        desc.maxLines = Integer.MAX_VALUE
    }

    /**
     * Clicking on QR Code icon will show QR code
     */
    private fun showQR() {
        //Initiate the barcode encoder
        val barcodeEncoder = BarcodeEncoder()
        //Encode text in editText into QRCode image into the specified size using barcodeEncoder
        val bitmap = barcodeEncoder.encodeBitmap(USER_ID+args.userId, BarcodeFormat.QR_CODE, resources.getInteger(
            R.integer.QR_size
        ), resources.getInteger(R.integer.QR_size))

        QRCode.shareQRcode(bitmap, requireActivity())

    }

    /**
     * sets up the 2 badges and the height achievements, if necessary
     */
    private fun bindBadgesAndAchievements(user: ProfileUser) {
        val badges = user.badges().sorted()
        var images = listOf<ImageView>(rootView.findViewById(R.id.badgePic1), rootView.findViewById(
            R.id.badgePic2
        ))
        when (badges.size) {
            0 -> images.forEach { it.visibility = INVISIBLE }
            1 -> {
                images[0].setImageResource(badges[0].imageID)
                images[0].setOnClickListener { printToast(getString(badges[0].descId)) }
                images[1].visibility = INVISIBLE
            }
            2 -> {
                images[0].setImageResource(badges[0].imageID)
                images[0].setOnClickListener { printToast(getString(badges[0].descId)) }
                images[1].setImageResource(badges[1].imageID)
                images[1].setOnClickListener { printToast(getString(badges[1].descId)) }
            }
        }
        val achFollowers = user.achievements().filter{it.cat == AchievementCategory.FOLLOWER}.sorted()
        images = listOf<ImageView>(rootView.findViewById(R.id.AchFollowing1), rootView.findViewById(
            R.id.AchFollowing2
        ),
            rootView.findViewById(R.id.AchFollowing3), rootView.findViewById(R.id.AchFollowing4))
        for (i in range(0, achFollowers.size)) {
            images[i].setImageResource(achFollowers[i].imageID)
            images[i].setOnClickListener { printToast(getString(achFollowers[i].descId)) }
        }
        val achFollowed = user.achievements().filter{it.cat == AchievementCategory.FOLLOWED}.sorted()
        images = listOf<ImageView>(rootView.findViewById(R.id.AchFollowed1), rootView.findViewById(R.id.AchFollowed2),
            rootView.findViewById(R.id.AchFollowed3), rootView.findViewById(R.id.AchFollowed4))
        for (i in range(0, achFollowed.size)) {
            images[i].setImageResource(achFollowed[i].imageID)
            images[i].setOnClickListener { printToast(getString(achFollowed[i].descId)) }
        }

    }

    /**
     * print a text in toast
     */
    private fun printToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
    }

    /**
     * When clicking on a meetup list element
     */
    private fun onListItemClick(position: Int) {
        val intent = Intent(requireContext(), MeetUpView::class.java)
            .apply {
                putExtra(
                    MEETUP_SHOWN,
                    (viewModel.meetupDataSet.value as Response.Success).data[position].uuid
                )
            }
        startActivity(intent)
    }
}