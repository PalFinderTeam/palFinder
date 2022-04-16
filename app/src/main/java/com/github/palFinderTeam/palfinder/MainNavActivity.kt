package com.github.palFinderTeam.palfinder

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.github.palFinderTeam.palfinder.ui.login.LoginActivity
import com.github.palFinderTeam.palfinder.ui.settings.SettingsActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainNavActivity : AppCompatActivity() {

    private var findState = FindState.MAP

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_nav)

        auth = Firebase.auth

        val navController =
            (supportFragmentManager.findFragmentById(R.id.main_content) as NavHostFragment).navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)


        navController.addOnDestinationChangedListener { _, _, arguments ->
            // Hide navbar when needed
            if (bottomNavigationView != null) {
                bottomNavigationView.isVisible = arguments?.getBoolean("ShowNavBar", false) == true
            }
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            val selected = bottomNavigationView.selectedItemId
            if (selected != item.itemId) {
                val direction = navItemToPosition(item.itemId) - navItemToPosition(selected)
                val animationIn = if (direction < 0) R.anim.slide_in_left else R.anim.slide_in_right
                val animationOut = if (direction < 0) R.anim.slide_out_right else R.anim.slide_out_left
                val navOptions = NavOptions.Builder()
                navOptions.setEnterAnim(animationIn).setExitAnim(animationOut)

                when (item.itemId) {
                    R.id.nav_bar_create -> {
                        navController.navigate(
                            R.id.creation_fragment,
                            args = null,
                            navOptions = navOptions.build()
                        )
                        return@setOnItemSelectedListener true
                    }
                    R.id.nav_bar_groups -> {
                        val args = Bundle().apply {
                            putBoolean("ShowOnlyJoined", true)
                            putBoolean("ShowFindTabs", false)
                        }
                        navController.navigate(
                            R.id.list_fragment,
                            args = args,
                            navOptions = navOptions.build()
                        )
                        return@setOnItemSelectedListener true
                    }
                    R.id.nav_bar_find -> {
                        navController.navigate(
                            R.id.find_fragment,
                            args = null,
                            navOptions = navOptions.build()
                        )
                        return@setOnItemSelectedListener true
                    }
                }
            }
            false
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.miLogout -> {
                //Logout the user
                auth.signOut()
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("341371843047-6i3a92lfmcb6555vsj9sb02tnhmkh4c8.apps.googleusercontent.com") //somehow cannot access value through google-service values.xml
                    .requestEmail()
                    .build()

                val client = GoogleSignIn.getClient(this, gso)
                client.signOut()
                val logoutIntent = Intent(this, LoginActivity::class.java)
                logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(logoutIntent)
                return true
            }
            R.id.miSettings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun navItemToPosition(itemId: Int): Int {
        return when (itemId) {
            R.id.nav_bar_create -> 0
            R.id.nav_bar_find -> 1
            R.id.nav_bar_groups -> 2
            else -> -1
        }
    }

    // Make sure we keep track of what mode of find was used.
    private enum class FindState {
        MAP,
        LIST
    }
}