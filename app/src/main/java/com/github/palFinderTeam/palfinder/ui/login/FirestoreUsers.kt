package com.github.palFinderTeam.palfinder.ui.login

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirestoreUsers {

    private val db = Firebase.firestore

    fun emailIsAvailable(email: String, tag: String): Boolean {
        var available: Boolean = true
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.w(tag, "Email already assigned")
                    available = false
                    break
                }
            }
            .addOnFailureListener { exception ->
                Log.w(tag, "Error getting documents: ", exception)
            }.isComplete
        return available
    }

    //add new user to the firestore users collection, replaced by ProfileUser
    /*
    fun addNewUser(user: FirebaseUser, dbUser: Cloneable, tag: String) {
        val docRef = db.collection("users").document(user.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.data != null) {
                    Log.d(tag, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(tag, "No such document")
                    db.collection("users")
                        .document(user.uid).set(dbUser, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d(tag, "DocumentSnapshot added with ID: ${user.uid}")
                        }
                        .addOnFailureListener { e ->
                            Log.w(tag, "Error adding document", e)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d(tag, "get failed with ", exception)
            }
    }
    */

}