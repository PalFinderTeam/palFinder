package com.github.palFinderTeam.palfinder.ui.login

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.navigation.MainNavActivity
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.user.settings.UserSettingsActivity
import com.github.palFinderTeam.palfinder.utils.createPopUp
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.google.android.gms.auth.api.identity.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

const val CREATE_ACCOUNT_PROFILE = "com.github.palFinderTeam.palFinder.CREATE_ACCOUNT_PROFILE"

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private lateinit var emailTv: TextView
    private lateinit var passwordTv: TextView

    @Inject
    lateinit var firebaseProfileService: ProfileService

    private var showOneTapUI = true

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_GOOGLE_SIGN_IN = 4926
        private const val REQ_ONE_TAP = 4  // Can be any integer unique to the Activity
        private const val REQUEST_CODE_GIS_SAVE_PASSWORD = 2 /* unique request id */

        const val HIDE_ONE_TAP = "LOGIN.HIDE_ONE_TAP"
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            lifecycleScope.launch {
                updateUI(currentUser)
            }
        }
    }

    // onCreate One Tap version
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        val theme = sharedPref.getInt("theme", R.style.palFinder_default_theme)
        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        showOneTapUI = !intent.getBooleanExtra(HIDE_ONE_TAP, false)

        val signInButton = findViewById<SignInButton>(R.id.signInButton)
        val signInOrRegister = findViewById<Button>(R.id.login)
        val noAccountButton = findViewById<Button>(R.id.noAccountButton)

        auth = Firebase.auth
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = beginSignInRequest()

        //disable auto fill to enable onetap save password
        window.decorView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS

        configureGoogleSignIn(signInButton)
        configurePasswordSignIn(signInOrRegister)
        configureNoAccountButton(noAccountButton)

        if (showOneTapUI) {
            displayOneTap()
        }
    }

    private fun configurePasswordSignIn(signInOrRegister: Button) {
        emailTv = findViewById(R.id.email)
        passwordTv = findViewById(R.id.password)

        signInOrRegister.setOnClickListener {
            val email = emailTv.text.toString()
            //no checks on password is made for now
            val password = passwordTv.text.toString()
            if (email == "" || password == "") {
                Toast.makeText(baseContext, "Enter both fields please.", Toast.LENGTH_SHORT).show()
            } else if (isValidEmail(email)) {
                createOrSignInAccount(email, password)
            } else {
                //pop "email not valid"
                Toast.makeText(baseContext, "Email not valid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configureGoogleSignIn(signInButton: SignInButton) {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) //somehow cannot access value through google-service values.xml
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(this, gso)
        signInButton.setOnClickListener {
            val signIntent = client.signInIntent
            startActivityForResult(signIntent, RC_GOOGLE_SIGN_IN)
        }
    }

    private fun isValidEmail(str: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(str).matches()
    }

    private fun beginSignInRequest() = BeginSignInRequest.builder()
        .setPasswordRequestOptions(
            BeginSignInRequest.PasswordRequestOptions.builder()
                .setSupported(true)
                .build()
        )
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(getString(R.string.default_web_client_id))
                // Only show accounts previously used to sign in.
                .setFilterByAuthorizedAccounts(true)
                .build()
        )
        .setAutoSelectEnabled(true)
        .build()

    private fun displayOneTap() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e ->
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                Log.d(TAG, e.localizedMessage)
            }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                oneTapRequestHandler(data)
            }
            RC_GOOGLE_SIGN_IN -> {
                googleSignInRequestHandler(data)
            }
            REQUEST_CODE_GIS_SAVE_PASSWORD -> {
                emailPasswordRequestHandler(resultCode)
            }
        }
    }

    private fun emailPasswordRequestHandler(resultCode: Int) {
        Log.d(TAG, "in save password result")
        if (resultCode == RESULT_OK) {
            /* password was saved */
            Toast.makeText(baseContext, "password saved", Toast.LENGTH_SHORT).show()
        } else if (resultCode == RESULT_CANCELED) {
            /* password saving was cancelled */
            Toast.makeText(baseContext, "password not saved", Toast.LENGTH_SHORT).show()
        }
        // Account created, we now sign in.
        signIn(emailTv.text.toString(), passwordTv.text.toString(), savePassword = false)
    }

    private fun googleSignInRequestHandler(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = task.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "Google sign in failed", e)
        }
    }

    private fun oneTapRequestHandler(data: Intent?) {
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken
            val username = credential.id
            val password = credential.password
            checkOneTapCredential(idToken, password, username)
        } catch (e: ApiException) {
            oneTapException(e)
        }
    }

    private fun createOrSignInAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                when {
                    task.isSuccessful -> {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        savePassword(email, password)
                    }
                    task.exception is FirebaseAuthUserCollisionException -> {
                        //if user exist already, go to sign in
                        signIn(email, password, true)
                    }
                    else -> {
                        //if creation fails, display error message
                        signInSignUpFailure(task, "createUserWithEmail:failure")
                    }
                }
            }
    }

    private fun signIn(email: String, password: String, savePassword: Boolean) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    if (savePassword) {
                        savePassword(email, password)
                    }
                    lifecycleScope.launch {
                        updateUI(auth.currentUser)
                    }

                } else {
                    // If sign in fails, display a message to the user.
                    signInSignUpFailure(task, "signInWithEmail:failure")
                }
            }
    }

    private fun signInSignUpFailure(task: Task<AuthResult>, logText: String) {
        Log.w(TAG, logText, task.exception)
        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            updateUI(null)
        }
    }

    private fun savePassword(email: String, password: String) {
        val signInPassword = SignInPassword(email, password)
        val savePasswordRequest =
            SavePasswordRequest.builder().setSignInPassword(signInPassword).build()
        Identity.getCredentialSavingClient(this).savePassword(savePasswordRequest)
            .addOnSuccessListener { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender,
                        REQUEST_CODE_GIS_SAVE_PASSWORD,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Couldn't save password: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e ->
                Log.d(TAG, e.localizedMessage)
                // No saved credentials found. Connect the user.
                signIn(emailTv.text.toString(), passwordTv.text.toString(), savePassword = false)
            }
    }


    private fun checkOneTapCredential(idToken: String?, password: String?, username: String?) {
        when {
            idToken != null -> {
                // Got an ID token from Google. Used to authenticate with the backend.
                Log.d(TAG, "Got ID token.")
                firebaseAuthWithGoogle(idToken)
            }
            password != null && username != null -> {
                // Got a saved username and password. Used to authenticate with the backend.
                Log.d(TAG, "Got password.")
                signIn(username, password, false)
            }
            else -> {
                // Shouldn't happen.
                Log.d(TAG, "No ID token or password!")
            }
        }
    }

    private fun oneTapException(e: ApiException) {
        when (e.statusCode) {
            CommonStatusCodes.CANCELED -> {
                Log.d(TAG, "One-tap dialog was closed.")
                // Don't re-prompt the user.
                showOneTapUI = false
            }
            CommonStatusCodes.NETWORK_ERROR -> {
                Log.d(TAG, "One-tap encountered a network error.")
                // Try again or just ignore.
            }
            else -> {
                Log.d(TAG, "Couldn't get credential from result." + " (${e.localizedMessage})")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    lifecycleScope.launch {
                        updateUI(user)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    signInSignUpFailure(task, "signInWithCredential:failure")
                }
            }
    }

    private suspend fun updateUI(user: FirebaseUser?) {
        //Navigate to Main Activity
        if (user == null) {
            Log.w(TAG, "Not user")
            return
        }

        if (firebaseProfileService.exists(user.uid)) {
            startActivity(Intent(this, MainNavActivity::class.java))
            finish()
        } else {
            val profileUser = ProfileUser(
                user.uid,
                "",
                "",
                "",
                Calendar.getInstance(),
                ImageInstance(user.photoUrl.toString())
            )

            val intent = Intent(this, UserSettingsActivity::class.java).apply {
                putExtra(CREATE_ACCOUNT_PROFILE, profileUser)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun configureNoAccountButton(noAccount: Button) {
        noAccount.setOnClickListener {
            createPopUp(this,
                {
                    startActivity(Intent(this, MainNavActivity::class.java))
                    finish()
                }
            )
        }
    }
}

