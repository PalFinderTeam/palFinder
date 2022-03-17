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
    val imgURL : String, var imgFetch : ImageFetcher? = null
) : Serializable, ViewModel(){

    companion object{
        const val NOT_LOADED = 0
        const val FILE_NOT_FOUND = 1
        const val CACHED = 2
    }

    private var bitmapCache : Bitmap? = null
    var isCached : Boolean = false

    // Solely to get the status of the image
    var imgStatus = NOT_LOADED

    /**
     * Loads the image asynchronously into the desired Image View
     * @param view ImageView that needs to be change
     */
    fun loadImageInto(view : ImageView){
        // Is image cached?
        if (isCached) {
            view.setImageBitmap(bitmapCache)
        } else {
            GlobalScope.launch(Dispatchers.Main){
                try {
                    view.setImageResource(android.R.color.background_dark)
                    view.alpha = 0.3f

                    withContext(Dispatchers.IO){
                        fetchFromDB()
                    }

                    imgStatus = CACHED
                    view.setImageBitmap(bitmapCache)
                } catch (e: Exception) {
                    view.setImageResource(R.drawable.not_found)
                    imgStatus = FILE_NOT_FOUND
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
            imgStatus = NOT_LOADED
            isCached = false
        }
    }

    /**
     * Load the online image with the correct tools
     */
    private suspend fun fetchFromDB(){
        if (imgFetch!=null) {
            bitmapCache = imgFetch!!.fetchImage()
            isCached = true
            return
        }
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