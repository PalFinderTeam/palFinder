package com.github.palFinderTeam.palfinder.utils.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URL

/**
 * Download an image as a bitmap from an HTTPS link
 * This is used for Google Account users, where the user image is
 * stored as an external file
 * @param imgURL String absolute url path to the image
 */
class ImageFetcherHttp(
    private val imgURL: String,
    private val input: InputStream? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ImageFetcher {

    private val TAG = "ImageFetcherHttps";

    override suspend fun fetchImage(): Bitmap? = withContext(dispatcher) {
        // NOTE: Can throw exception, deal with it in the activity
        val inputStream = URL(imgURL).openStream()
        val bufferedInputStream = BufferedInputStream(inputStream)
        val bitmap = BitmapFactory.decodeStream(bufferedInputStream)
        inputStream.close()
        bitmap
    }

    private fun getInputStream(): InputStream {

        if (input != null) return input

        return URL(imgURL).openStream()
    }

}