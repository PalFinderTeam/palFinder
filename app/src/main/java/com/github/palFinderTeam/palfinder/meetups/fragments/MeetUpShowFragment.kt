package com.github.palFinderTeam.palfinder.meetups.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.chat.CHAT
import com.github.palFinderTeam.palfinder.chat.ChatActivity
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpViewViewModel
import com.github.palFinderTeam.palfinder.profile.ProfileListFragment
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsViewModel
import com.github.palFinderTeam.palfinder.tag.TagsViewModelFactory
import com.github.palFinderTeam.palfinder.utils.addTagsToFragmentManager
import com.github.palFinderTeam.palfinder.utils.createTagFragmentModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeetUpShowFragment : Fragment() {

    private val viewModel: MeetUpViewViewModel by activityViewModels()
    private lateinit var tagsViewModelFactory: TagsViewModelFactory<Category>
    private lateinit var tagsViewModel: TagsViewModel<Category>
    private lateinit var binding: View

    private val args: MeetUpShowFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflate(R.layout.fragment_meetup_show, container, false)
        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val meetupId = args.meetupId
        viewModel.loadMeetUp(meetupId)
        val button = view.findViewById<Button>(R.id.show_profile_list_button)
        button.setOnClickListener { showProfileList() }

        viewModel.meetUp.observe(viewLifecycleOwner) {
            fillFields()
            handleButton()
        }

        tagsViewModelFactory = TagsViewModelFactory(viewModel.tagRepository)
        tagsViewModel = createTagFragmentModel(this, tagsViewModelFactory)
        if (savedInstanceState == null) {
            addTagsToFragmentManager(childFragmentManager, R.id.fc_tags)
        }
    }

    private fun handleButton() {
        val hasJoined = viewModel.hasJoin()
        val isCreator = viewModel.isCreator()
        binding.findViewById<View>(R.id.bt_ChatMeetup).apply {
            this.isEnabled = hasJoined
            this.isVisible = hasJoined
            this.isClickable = hasJoined
        }
        binding.findViewById<View>(R.id.bt_EditMeetup).apply {
            this.isEnabled = isCreator
            this.isVisible = isCreator
            this.isClickable = isCreator
        }
        binding.findViewById<Button>(R.id.bt_JoinMeetup).apply {
            this.isEnabled = !isCreator
            this.isClickable = !isCreator
            this.text =
                if (hasJoined) getString(R.string.meetup_view_leave) else getString(R.string.meetup_view_join)
        }
    }

    private fun showProfileList() {
        ProfileListFragment(viewModel.meetUp.value?.participantsId!!).show(
            childFragmentManager,
            "profile list"
        )
    }

    private fun fillFields() {
        tagsViewModel.refreshTags()
    }

    fun onEdit(v: View) {
        if (viewModel.isCreator()) {
            val action = MeetUpShowFragmentDirections.actionMeetupShowFragmentToMeetupCreationFragment()
            findNavController().navigate(action)
        }
    }

    fun onChat(v: View) {
        if (viewModel.hasJoin()) {
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra(CHAT, viewModel.getMeetupID())
            }
            startActivity(intent)
        }
    }

    fun onJoinOrLeave(v: View) {
        viewModel.joinOrLeave(requireContext())
    }
}