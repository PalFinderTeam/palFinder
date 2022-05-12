package com.github.palFinderTeam.palfinder.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.ProfileActivity
import com.github.palFinderTeam.palfinder.ProfileViewModel
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.SearchedFilter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileListFragment(private val usersId: List<String>) : DialogFragment() {
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //inflate layout with recycler view
        val v: View = inflater.inflate(R.layout.fragment_profile_list, container, false)
        recyclerView = v.findViewById(R.id.profile_list_recycler)
        viewModel.profilesList.observe(this) {
            changeAdapter(it, v)
        }
        viewModel.fetchUsersProfile(usersId)
        return v
    }

    override fun onStart() {
        super.onStart()

        // Force the dialog to take whole width
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        );
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    private fun onListItemClick(position: Int) {
        val intent = Intent(activity?.applicationContext, ProfileActivity::class.java)
            .apply { putExtra(USER_ID, (recyclerView.adapter as ProfileAdapter).currentDataSet[position].uuid) }
        startActivity(intent)
    }

    private fun changeAdapter(list: List<ProfileUser>, v: View) {
        viewModel.fetchLoggedProfile()
        viewModel.logged_profile.observe(this) {
            if (it is Response.Success) {
                val adapter = ProfileAdapter(
                    list,
                    it.data,
                    requireContext(),
                    ::onListItemClick,
                    { id -> viewModel.follow(it.data.uuid, id) },
                    { id -> viewModel.unFollow(it.data.uuid, id) })
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = adapter
                val searchField = v.findViewById<SearchView>(R.id.profile_list_search)
                searchField.imeOptions = EditorInfo.IME_ACTION_DONE
                SearchedFilter.setupSearchField(searchField, adapter.filter)
            }
        }
    }
}