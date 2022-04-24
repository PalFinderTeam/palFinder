package com.github.palFinderTeam.palfinder.utils.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.github.palFinderTeam.palfinder.utils.image.ImageFetcher.Companion.TMP_NAME
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Download a Firebase image as a bitmap
 * This is used for anyone who uploaded a custom profile
 * picture to our database
 * @param imgURL String url path inside Firebase storage
 * @param dispatcher The coroutine dispatcher to use to perform the async task, by default [Dispatchers.IO].
 */
class ImageFetcherFirebase(
    private val imgURL: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ImageFetcher {

    override suspend fun fetchImage(): Bitmap = withContext(dispatcher) {
        val storageRef = FirebaseStorage.getInstance().reference.child(imgURL)

        // Create a temporary local file
        val localFile = File.createTempFile(TMP_NAME, UrlFormat.getUrlExtension(imgURL))

        // Async block thread
        storageRef.getFile(localFile).await()
        BitmapFactory.decodeFile(localFile.absolutePath)
    }

}