package com.github.palFinderTeam.palfinder.utils.image

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class FirebaseImageUploader @Inject constructor(
    private val storage: FirebaseStorage
) : ImageUploader {

    override suspend fun uploadImage(imageUri: Uri): String? {
        val fileName = ICONS_FOLDER + UUID.randomUUID().toString() + ".jpg"
        return try {
            storage.reference.child(fileName).putFile(imageUri).await()
            fileName
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun removeImage(imagePath: String) {
        storage.reference.child(imagePath).delete().await()
    }

    companion object {
        const val ICONS_FOLDER = "icons/"
    }
}