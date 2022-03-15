package com.github.palFinderTeam.palfinder.ui.login

//import android.text.Editable
//import android.text.TextWatcher
//import android.widget.EditText
//import android.widget.Toast
//import androidx.annotation.StringRes

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.autofill.AutofillManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.MainActivity
import com.github.palFinderTeam.palfinder.R
import com.google.android.gms.auth.api.identity.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    //private lateinit var signInButton: FrameLayout
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest


    private companion object{
        private const val TAG = "LoginActivity"
        private const val RC_GOOGLE_SIGN_IN = 4926
        private val REQ_ONE_TAP = 4  // Can be any integer unique to the Activity
        private const val REQUEST_CODE_GIS_SAVE_PASSWORD = 2 /* unique request id */
        private var showOneTapUI = true
        val db = Firebase.firestore
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            updateUI(currentUser)
        }
    }

 /* Previous google sign in version
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth
        val signInButton = findViewById<SignInButton>(R.id.signInButton)


        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(GID) //somehow cannot access value through google-service values.xml
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(this, gso)
        signInButton.setOnClickListener{
            val signIntent = client.signInIntent
            startActivityForResult(signIntent, RC_GOOGLE_SIGN_IN)
        }
    }
    */

    // onCreate One Tap version
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val signInButton = findViewById<SignInButton>(R.id.signInButton)
        val signInOrRegister = findViewById<Button>(R.id.login)
        auth = Firebase.auth
        oneTapClient = Identity.getSignInClient(this)
        signInRequest= beginSignInRequest()
        displayOneTap()

        //disable auto fill to enable onetap save password, work only with API >= 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window
                .decorView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }


        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) //somehow cannot access value through google-service values.xml
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(this, gso)
        signInButton.setOnClickListener{
            val signIntent = client.signInIntent
            startActivityForResult(signIntent, RC_GOOGLE_SIGN_IN)
        }
        signInOrRegister.setOnClickListener {
            val email = findViewById<TextView>(R.id.email).text.toString()
            //no checks on password is made for now
            val password= findViewById<TextView>(R.id.password).text.toString()
            if (isValidEmail(email)) {
                if (emailIsAvailability(email)){
                    createAccount(email, password)
                }else{
                    signIn(email, password)
                }
            }
            else {
                //pop "email not valid"
                Toast.makeText(baseContext, "Email not valid",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    //could be moved in db utils
    private fun emailIsAvailability(email: String): Boolean {
        var available: Boolean = true
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.w(TAG, "Email already assigned")
                    available = false
                    break
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }.isComplete
        return available
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
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    val username = credential.id
                    val password = credential.password
                    checkOneTapCredential(idToken, password)
                } catch (e: ApiException) {
                    oneTapException(e)
                }
            }
            RC_GOOGLE_SIGN_IN -> {
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
            REQUEST_CODE_GIS_SAVE_PASSWORD -> {
                Log.d(TAG, "in save password result")
                if (resultCode == Activity.RESULT_OK) {
                    /* password was saved */
                    Toast.makeText(
                        baseContext, "password saved",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    /* password saving was cancelled */
                    Toast.makeText(
                        baseContext, "password not saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                updateUI(auth.currentUser)
            }
        }
    }

    private fun createAccount(email: String, password: String) {
        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    savePassword(email,password)
                } else {
                    // If user already in database, sign in
                    if(task.exception is FirebaseAuthUserCollisionException){
                        signIn(email, password)
                    } else {
                        //if creation fails, display error message
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        updateUI(null)
                    }
                }
            }
        // [END create_user_with_email]
    }

    private fun signIn(email: String, password: String) {
        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    savePassword(email,password)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
        // [END sign_in_with_email]
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



    private fun checkOneTapCredential(idToken: String?, password: String?) {
        when {
            idToken != null -> {
                // Got an ID token from Google. Use it to authenticate
                // with your backend.
                Log.d(TAG, "Got ID token.")
                firebaseAuthWithGoogle(idToken)
            }
            password != null -> {
                // Got a saved username and password. Use them to authenticate
                // with your backend.
                Log.d(TAG, "Got password.")
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
                Log.d(
                    TAG, "Couldn't get credential from result." +
                            " (${e.localizedMessage})"
                )
            }
        }
    }
    /* previous onActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
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
    }*/

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        //Navigate to Main Activity
        if(user == null){
            Log.w(TAG, "Not user")
            return
        }
        val dbUser = hashMapOf(
            "name" to user.displayName,
            "email" to user.email,
            "join_date" to Date(),
            "picture" to user.photoUrl.toString()
        )
        addNewUser(user, dbUser)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    //could be moved into db utils
    private fun addNewUser(user: FirebaseUser, dbUser: Cloneable) {
        val docRef = db.collection("users").document(user.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.data != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                    db.collection("users")
                        .document(user.uid).set(dbUser, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot added with ID: ${user.uid}")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    /*private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }*/
}

/*
/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}*/
