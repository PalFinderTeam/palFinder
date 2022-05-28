package com.github.palFinderTeam.palfinder.navigation

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.github.palFinderTeam.palfinder.PalFinderBaseActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpView
import com.github.palFinderTeam.palfinder.profile.ProfileFragment
import com.github.palFinderTeam.palfinder.profile.ProfileFragment.Companion.PROFILE_ID_ARG
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.USER_ID
import com.github.palFinderTeam.palfinder.ui.login.LoginActivity
import com.github.palFinderTeam.palfinder.ui.login.LoginActivity.Companion.HIDE_ONE_TAP
import com.github.palFinderTeam.palfinder.ui.settings.SettingsActivity
import com.github.palFinderTeam.palfinder.user.settings.UserSettingsActivity
import com.github.palFinderTeam.palfinder.utils.createNoAccountPopUp
import com.github.palFinderTeam.palfinder.utils.createPopUp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainNavActivity : PalFinderBaseActivity() {

    companion object {
        const val SHOW_NAVBAR_ARG = "ShowNavBar"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNavigationView: BottomNavigationView

    @Inject
    lateinit var profileService: ProfileService

    @Inject
    lateinit var meetUpRepository: MeetUpRepository

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_nav)

        auth = Firebase.auth

        navController =
            (supportFragmentManager.findFragmentById(R.id.main_content) as NavHostFragment).navController
        bottomNavigationView = findViewById(R.id.bottom_nav)


        navController.addOnDestinationChangedListener { _, _, arguments ->
            // Hide navbar when needed
            hideShowNavBar(arguments?.getBoolean(SHOW_NAVBAR_ARG, false) == true)
        }

        // Make sure that selected item is the one displayed to the user
        navController.currentDestination?.let {
            when (it.id) {
                R.id.find_fragment -> bottomNavigationView.selectedItemId = R.id.nav_bar_find
                R.id.profile_fragment -> bottomNavigationView.selectedItemId = R.id.nav_bar_profile
                R.id.creation_fragment -> bottomNavigationView.selectedItemId = R.id.nav_bar_create
            }
        }

        val animateLeftOptions = transitionAnimation(R.anim.slide_in_right, R.anim.slide_out_left)
        val animateRightOptions = transitionAnimation(R.anim.slide_in_left, R.anim.slide_out_right)

        // Bottom navigation behaviour
        bottomNavigationView.setOnItemSelectedListener { item ->
            val selected = bottomNavigationView.selectedItemId
            if (selected != item.itemId) {
                val direction = navItemToPosition(item.itemId) - navItemToPosition(selected)
                val options = if (direction < 0) animateRightOptions else animateLeftOptions


                when (item.itemId) {
                    R.id.nav_bar_create -> {
                        if (profileService.getLoggedInUserID() == null) {
                            createNoAccountPopUp(this, R.string.no_account_create)
                            return@setOnItemSelectedListener false
                        } else {
                            navBarNavigate(R.id.creation_fragment, null, options)
                        }
                    }
                    R.id.nav_bar_profile -> {
                        val loggedUser = profileService.getLoggedInUserID()
                        if (loggedUser == null) {
                            createNoAccountPopUp(this, R.string.no_account_profile)
                            return@setOnItemSelectedListener false
                        } else {
                            val args = Bundle().apply {
                                putSerializable(PROFILE_ID_ARG, loggedUser)
                            }
                            navBarNavigate(R.id.profile_fragment, args, options)
                        }
                    }
                    R.id.nav_bar_find -> {
                        navBarNavigate(R.id.find_fragment, null, options)
                    }
                }
            }
            true
        }

    }

    /**
     * Navigate to one of the nav bar tab
     */
    private fun navBarNavigate(fragmentId: Int, args: Bundle?, navOptions: NavOptions) {
        navController.popBackStack()
        navController.navigate(
            fragmentId,
            args = args,
            navOptions = navOptions
        )
    }
    /**
     * Create the necessary options to animate a transition
     */
    private fun transitionAnimation(enterId: Int, exitId: Int): NavOptions {
        return navOptions {
            this.anim {
                enter = enterId
                exit = exitId
            }
        }
    }

    fun hideShowNavBar(show: Boolean) {
        bottomNavigationView.isVisible = show
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    // Register the launcher and result handler for the qr code reader
    private val barcodeLauncher: ActivityResultLauncher<ScanOptions?> = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            val originalIntent = result.originalIntent
            if (originalIntent == null) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.cancelled_scan),
                    Toast.LENGTH_LONG
                ).show()
            } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.no_camera_permission_message),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                applicationContext,
                getString(R.string.scanned) + ": " + result.contents,
                Toast.LENGTH_LONG
            ).show()
            if (result.contents.startsWith(USER_ID)) {
                createPopUp(
                    this,
                    textId = R.string.qr_scan_follow_account,
                    continueButtonTextId = R.string.follow
                )
                {
                    CoroutineScope(Dispatchers.IO).launch {
                        profileService.followUser(
                            profileService.fetch(profileService.getLoggedInUserID()!!)!!,
                            result.contents.removePrefix(USER_ID)
                        )
                    }.invokeOnCompletion {
                        val intent = Intent(this, ProfileFragment::class.java)
                            .apply { putExtra(USER_ID, result.contents.removePrefix(USER_ID)) }
                        startActivity(intent)
                    }
                }
            } else {
                createPopUp(
                    this,
                    textId = R.string.qr_scan_join_meetup,
                    continueButtonTextId = R.string.meetup_view_join
                )
                {
                    CoroutineScope(Dispatchers.IO).launch {
                        meetUpRepository.joinMeetUp(
                            result.contents.removePrefix(MEETUP_SHOWN),
                            profileService.getLoggedInUserID()!!, Calendar.getInstance(),
                            profileService.fetch(profileService.getLoggedInUserID()!!)!!
                        )
                    }.invokeOnCompletion {
                        val intent = Intent(this, MeetUpView::class.java)
                            .apply {
                                putExtra(
                                    MEETUP_SHOWN, result.contents.removePrefix(
                                        MEETUP_SHOWN
                                    )
                                )
                            }
                        startActivity(intent)
                    }
                }
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miLogout -> {
                //Logout the user
                auth.signOut()
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("341371843047-6i3a92lfmcb6555vsj9sb02tnhmkh4c8.apps.googleusercontent.com") //somehow cannot access value through google-service values.xml
                    .requestEmail()
                    .build()

                val client = GoogleSignIn.getClient(this, gso)
                client.signOut()
                val logoutIntent =
                    Intent(this, LoginActivity::class.java).apply { putExtra(HIDE_ONE_TAP, true) }
                logoutIntent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(logoutIntent)
            }
            R.id.miSettings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.miUserSettings -> {
                if (profileService.getLoggedInUserID() == null) {
                    createNoAccountPopUp(this, R.string.no_account_profile)
                } else {
                    //super.onOptionsItemSelected(item)
                    startActivity(Intent(this, UserSettingsActivity::class.java))
                }
            }
            R.id.miScanQR -> {
                val options = ScanOptions()
                options.setOrientationLocked(false);
                barcodeLauncher.launch(options)
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    private fun navItemToPosition(itemId: Int): Int {
        return when (itemId) {
            R.id.nav_bar_create -> 0
            R.id.nav_bar_find -> 1
            R.id.nav_bar_profile -> 2
            else -> -1
        }
    }

}