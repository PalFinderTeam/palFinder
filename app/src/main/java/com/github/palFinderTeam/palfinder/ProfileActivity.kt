package com.github.palFinderTeam.palfinder

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ceylonlabs.imageviewpopup.ImagePopup
import com.github.palFinderTeam.palfinder.meetups.MeetupListRootAdapter
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpView
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.profile.USER_ID
import com.github.palFinderTeam.palfinder.utils.Response
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeEncoder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


/**
 * When creating the profile page, the username (which is unique) will
 * be sent to from the previous page as an intent. A database query will be made
 * and the user info will be sent back
 */
@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private lateinit var meetupList: RecyclerView
    private lateinit var adapter: MeetupListRootAdapter
    private lateinit var intentIntegrator: IntentIntegrator

    private val viewModel: ProfileViewModel by viewModels()
    companion object{
        const val EMPTY_FIELD = ""
        const val MAX_SHORT_BIO_DISPLAY_LINES = 2
        const val FOLLOWERS: String = "%d followers"
        const val FOLLOWING: String = "following %d"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        if (intent.hasExtra(USER_ID)) {
            val userId = intent.getStringExtra(USER_ID)!!
            viewModel.fetchProfile(userId)
            viewModel.fetchLoggedProfile()

            // Fetch last Meetups list through adapter
            meetupList = this.findViewById(R.id.meetup_list_recycler)
            meetupList.layoutManager = LinearLayoutManager(this)

            viewModel.fetchUserMeetups(userId)

            // Bind the adapter to the RecyclerView
            viewModel.meetupDataSet.observe(this) { dataResp ->
                if (dataResp is Response.Success) {
                    val meetups = dataResp.data
                    adapter = MeetupListRootAdapter(
                        meetups,
                        meetups.toMutableList(),
                        context = applicationContext
                    ) { onListItemClick(it) }
                    meetupList.adapter = adapter
                }
            }
        }

        intentIntegrator = IntentIntegrator(this)

        viewModel.profile.observe(this) {
            when(it) {
                is Response.Success -> {
                    injectUserInfo(it.data)
                    bindFollow(null, it.data)
                }
                is Response.Loading -> Toast.makeText(applicationContext, "Fetching",  Toast.LENGTH_LONG).show()
                is Response.Failure -> Toast.makeText(applicationContext, it.errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * binds the follow/unfollow button
     */
    private fun bindFollow(view: View?, profileViewed: ProfileUser) {
        val followButton = findViewById<Button>(R.id.button_join_meetup)
        val blockButton = findViewById<Button>(R.id.blackList)
        viewModel.logged_profile.observe(this) {
            when(it) {
                is Response.Success -> {
                   followAndBlockSystem(it.data, profileViewed, followButton, blockButton)
                }
                is Response.Loading -> Toast.makeText(applicationContext, "Fetching",  Toast.LENGTH_LONG).show()
                is Response.Failure -> Toast.makeText(applicationContext, it.errorMessage, Toast.LENGTH_LONG).show()
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
        findViewById<TextView>(R.id.userProfileUsername).text = user.atUsername()
        findViewById<TextView>(R.id.userProfileJoinDate).apply { text = user.prettyJoinTime() }
        findViewById<TextView>(R.id.followers).text = String.format(FOLLOWERS, user.followed.size)
        findViewById<TextView>(R.id.following).text = String.format(FOLLOWING, user.following.size)
        if(user.canProfileBeSeenBy(viewModel.profileService.getLoggedInUserID()!!)) {
            findViewById<TextView>(R.id.userProfileName).text = user.fullName()
            injectBio(user.description)
        }else{
            findViewById<TextView>(R.id.userProfileName).text = this.resources.getString(R.string.private_name)
            injectBio(this.resources.getString(R.string.private_desc))
        }
        lifecycleScope.launch {
            user.pfp.loadImageInto(findViewById(R.id.userProfileImage), applicationContext)
        }
    }


    /**
     * Injects the bio by applying the Read More feature
     *
     * @param bio: String
     */
    private fun injectBio(bio: String) {
        val desc = findViewById<TextView>(R.id.userProfileDescription)
        if (bio == EMPTY_FIELD) {
            findViewById<TextView>(R.id.userProfileAboutTitle).text = this.resources.getString(R.string.no_desc)
            desc.text = EMPTY_FIELD
            showFullDesc(null)
        } else {
            desc.text = bio
            desc.post {
                val lineCount: Int = desc.lineCount
                if (lineCount < MAX_SHORT_BIO_DISPLAY_LINES) {
                    showFullDesc(null)
                }
            }
        }
    }

    /**
     * Clicking on Read More will reveal the entire text
     */
    fun showFullDesc(view: View?) {
        val overflow = findViewById<TextView>(R.id.userProfileDescOverflow)
        overflow.visibility = GONE
        val desc = findViewById<TextView>(R.id.userProfileDescription)
        desc.maxLines = Integer.MAX_VALUE
    }

    /**
     * Clicking on QR Code icon will show QR code
     */
    fun showQR(view: View?) {
        //Initiate the barcode encoder
        val barcodeEncoder = BarcodeEncoder()
        //Encode text in editText into QRCode image into the specified size using barcodeEncoder
        val bitmap = barcodeEncoder.encodeBitmap(USER_ID+intent.getStringExtra(USER_ID), BarcodeFormat.QR_CODE, resources.getInteger(R.integer.QR_size), resources.getInteger(R.integer.QR_size))

        //Set up the popup image
        val imagePopup = ImagePopup(this)
        //Convert the bitmap(QR Code) into a drawable
        val d: Drawable = BitmapDrawable(resources, bitmap)

        //Displays the popup image
        imagePopup.initiatePopup(d);
        imagePopup.viewPopup()

    }

    /**
     * When clicking on a meetup list element
     */
    private fun onListItemClick(position: Int) {
        val intent = Intent(this, MeetUpView::class.java)
            .apply {
                putExtra(
                    MEETUP_SHOWN,
                    (viewModel.meetupDataSet.value as Response.Success).data[position].uuid
                )
            }
        startActivity(intent)
    }


}