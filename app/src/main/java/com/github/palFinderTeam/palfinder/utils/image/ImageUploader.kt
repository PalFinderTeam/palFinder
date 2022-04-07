package com.github.palFinderTeam.palfinder.utils.image

import android.net.Uri

interface ImageUploader {
    /**
     * Upload an image to a database.
     *
     * @param imageUri The Uri of the image.
     *
     * @return The filepath on the db or null if something wrong happens.
     */
    suspend fun uploadImage(imageUri: Uri): String?

    /**
     * Remove an image from the database.
     *
     * @param imagePath The path of the image on the database.
     */
    suspend fun removeImage(imagePath: String)
}