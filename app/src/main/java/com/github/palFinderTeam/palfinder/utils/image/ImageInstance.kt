package com.github.palFinderTeam.palfinder.utils.image

import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.ViewModel
import com.github.palFinderTeam.palfinder.R
import kotlinx.coroutines.*
import java.io.File
import java.io.Serializable
import java.lang.Exception

/**
 * Download an image from a given URL and injects it into an
 * `ImageView`. This can be a Firebase storage image or a raw
 * HTTP(S) link
 * @param imgURL String image URL path
 */
data class ImageInstance(
    val imgURL : String
) : Serializable, ViewModel(){

    private val TAG = "ImageInstance";

    private lateinit var localFile : File
    private var bitmapCache : Bitmap? = null
    var isCached : Boolean = false

    /**
     * Loads the image asynchronously into the desired Image View
     * @param view ImageView that needs to be change
     */
    fun loadImageInto(view : ImageView){
        // Is image cached?
        if (isCached) {
            Log.d(TAG, "Image is already existing!")
            view.setImageBitmap(bitmapCache)
        } else {
            Log.d(TAG, "Fetching from Database")

            GlobalScope.launch(Dispatchers.Main){
                try {
                    view.setImageResource(android.R.color.background_dark)
                    view.alpha = 0.3f

                    withContext(Dispatchers.IO){
                        fetchFromDB()
                    }

                    view.setImageBitmap(bitmapCache)
                } catch (e: Exception) {
                    view.setImageResource(R.drawable.not_found)
                    isCached = false
                } finally {
                    view.alpha = 1.0f
                }
            }
        }
    }

    /**
     * Clears cached image, forces to re-download image when
     * loading image into another view
     */
    fun clearImageCache(){
        if (isCached) {
            bitmapCache = null
            isCached = false
        }
    }

    /**
     * Load the online image with the correct tools
     */
    private suspend fun fetchFromDB(){
        // Select corresponding fetcher
        val fetcher : ImageFetcher = if (UrlFormat.getUrlType(imgURL) == UrlFormat.URL_IS_FIREBASE) {
            ImageFetcherFirebase(imgURL)
        } else {
            ImageFetcherHttp(imgURL)
        }
        bitmapCache = fetcher.fetchImage()
        isCached = true
    }
}