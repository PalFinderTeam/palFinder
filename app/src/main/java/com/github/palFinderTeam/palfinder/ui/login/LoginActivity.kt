package com.github.palFinderTeam.palfinder.ui.login

//import android.text.Editable
//import android.text.TextWatcher
//import android.widget.EditText
//import android.widget.Toast
//import androidx.annotation.StringRes

//import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.palFinderTeam.palfinder.navigation.MainNavActivity
import com.github.palFinderTeam.palfinder.R
import com.google.android.gms.auth.api.identity.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.github.palFinderTeam.palfinder.profile.FirebaseProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.user.settings.UserSettingsActivity
import com.github.palFinderTeam.palfinder.utils.createPopUp
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.firestore.SetOptions
//import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

const val CREATE_ACCOUNT_PROFILE = "com.github.palFinderTeam.palFinder.CREATE_ACCOUNT_PROFILE"
var IS_NO_ACCOUNT_USER = false

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    //private lateinit var signInButton: FrameLayout
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest


    private companion object{
        private const val TAG = "LoginActivity"
        private const val RC_GOOGLE_SIGN_IN = 4926
        private const val REQ_ONE_TAP = 4  // Can be any integer unique to the Activity
        private const val REQUEST_CODE_GIS_SAVE_PASSWORD = 2 /* unique request id */
        private var showOneTapUI = true
        var firebaseProfileService = FirebaseProfileService(Firebase.firestore)
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
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

        val signInButton = findViewById<SignInButton>(R.id.signInButton)
        val signInOrRegister = findViewById<Button>(R.id.login)
        val noAccountButton = findViewById<Button>(R.id.noAccountButton)

        auth = Firebase.auth
        oneTapClient = Identity.getSignInClient(this)
        signInRequest= beginSignInRequest()
        displayOneTap()

        //disable auto fill to enable onetap save password, work only with API >= 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window
                .decorView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }
        configureGoogleSignIn(signInButton)
        configurePasswordSignIn(signInOrRegister)
        configureNoAccountButton(noAccountButton)
    }

    private fun configurePasswordSignIn(signInOrRegister: Button) {
        signInOrRegister.setOnClickListener {
            val email = findViewById<TextView>(R.id.email).text.toString()
            //no checks on password is made for now
            val password = findViewById<TextView>(R.id.password).text.toString()
            if(email == "" || password == ""){
                Toast.makeText(baseContext, "Enter both fields please.", Toast.LENGTH_SHORT).show()
            }
            else if (isValidEmail(email)) {
                createAccount(email, password)
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

    private fun isValidEmail(str: String): Boolean{
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

    private fun displayOneTap(){
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null)
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
        lifecycleScope.launch {
            updateUI(auth.currentUser)
        }
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

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                when {
                    task.isSuccessful -> {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        savePassword(email,password)
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
        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    if(savePassword) {
                        savePassword(email,password)
                    }
                    lifecycleScope.launch {
                        updateUI(auth.currentUser)
                    }

                } else {
                    // If sign in fails, display a message to the user.
                    signInSignUpFailure(task, "signInWithEmail:failure")
                }
            }
        // [END sign_in_with_email]
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
                        REQUEST_CODE_GIS_SAVE_PASSWORD,  /* fillInIntent= */
                        null,  /* flagsMask= */
                        0,  /* flagsValue= */
                        0,  /* extraFlags= */
                        0,  /* options= */
                        null
                    )}catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Couldn't save password: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e ->
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                Log.d(TAG, e.localizedMessage)
            }
    }



    private fun checkOneTapCredential(idToken: String?, password: String?, username: String?) {
        when {
            idToken != null -> {
                // Got an ID token from Google. Used to authenticate with the backend.
                Log.d(TAG, "Got ID token.")
                firebaseAuthWithGoogle(idToken)
            }
            password != null && username != null-> {
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

        if (firebaseProfileService.doesUserIDExist(user.uid)) {
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

            //firestoreUsers.addNewUser(user, dbUser, TAG)

            val intent = Intent(this, UserSettingsActivity::class.java).apply {
                putExtra(CREATE_ACCOUNT_PROFILE, profileUser)
            }
            startActivity(intent)
            finish()
        }
    }
    /*private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }*/

    private fun configureNoAccountButton(noAccount: Button){
        noAccount.setOnClickListener {

            createPopUp(this,
                findViewById(R.id.container),
                {
                    IS_NO_ACCOUNT_USER = true
                    startActivity(Intent(this, MainNavActivity::class.java))
                    finish()
                }
            )

        }
    }
}

