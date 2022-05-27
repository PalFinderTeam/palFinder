package com.github.palFinderTeam.palfinder.utils.generics

/**
 * Object that can be put in firebase
 */
interface FirebaseObject {
    /**
     * @return a representation which is Firestore friendly.
     */
    fun toFirestoreData(): HashMap<String, Any?>

    /**
     * @return uuid of the object
     */
    fun getUUID(): String
}