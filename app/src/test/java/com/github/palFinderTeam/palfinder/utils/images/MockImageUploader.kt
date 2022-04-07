package com.github.palFinderTeam.palfinder.utils.images

import android.net.Uri
import com.github.palFinderTeam.palfinder.utils.image.ImageUploader

class MockImageUploader : ImageUploader {
    private val storage: HashMap<String, Uri> = HashMap()
    override suspend fun uploadImage(imageUri: Uri): String? {
        storage[imageUri.toString()] = imageUri
        return imageUri.toString()
    }

    override suspend fun removeImage(imagePath: String) {
        storage.remove(imagePath)
    }

    fun clearDB() {
        storage.clear()
    }
}