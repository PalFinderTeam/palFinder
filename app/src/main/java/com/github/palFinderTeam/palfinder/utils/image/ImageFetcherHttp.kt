package com.github.palFinderTeam.palfinder.utils.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Download an image as a bitmap from an HTTPS link
 * This is used for Google Account users, where the user image is
 * stored as an external file
 * @param imgURL String absolute url path to the image
 */
class ImageFetcherHttp(var imgURL: String) : ImageFetcher {

    private val TAG = "ImageFetcherHttps";

    override suspend fun fetchImage(): Bitmap? {
        val bufferedInputStream = withContext(Dispatchers.IO) {

            val url = URL(imgURL)
            val connection: HttpURLConnection?

            connection = url.openConnection() as HttpURLConnection
            connection.connect()

            // NOTE: Can throw exception, deal with it in the activity
            val inputStream: InputStream = connection.inputStream
            BufferedInputStream(inputStream)
        }

        return BitmapFactory.decodeStream(bufferedInputStream)
    }

}