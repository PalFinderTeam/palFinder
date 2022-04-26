package com.github.palFinderTeam.palfinder.utils.image

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.ViewModel
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.cache.FileCache
import com.github.palFinderTeam.palfinder.utils.EspressoIdlingResource
import java.io.Serializable
import java.lang.Exception

/**
 * Download an image from a given URL and injects it into an
 * `ImageView`. This can be a Firebase storage image or a raw
 * HTTP(S) link.
 * @param imgURL String image URL path If URL is empty, then
 * return an empty image file.
 */
data class ImageInstance(
    val imgURL : String, val imgFetch : ImageFetcher? = null
) : Serializable, ViewModel() {

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
     * @param view ImageView that needs to be change.
     */
    suspend fun loadImageInto(view : ImageView, context: Context){
        val cache = FileCache(
            imgURL,
            Bitmap::class.java,
            permanent = false,
            context = context
        )
        if (cache.exist()) {
            view.setImageBitmap(cache.get())
        } else {
            if(imgURL != "") {
                EspressoIdlingResource.increment()
                try {
                    view.setImageResource(android.R.color.background_dark)
                    view.alpha = 0.3f

                    val bitmapCache = fetchFromDB()
                    imgStatus = CACHED
                    view.setImageBitmap(bitmapCache)
                    cache.store(bitmapCache!!)
                } catch (e: Exception) {
                    Log.e("fuck", e.toString())
                    view.setImageResource(R.drawable.not_found)
                    imgStatus = FILE_NOT_FOUND
                    isCached = false
                } finally {
                    view.alpha = 1.0f
                    EspressoIdlingResource.decrement()
                }
            } else {
                view.setImageResource(R.drawable.not_found)
                imgStatus = FILE_NOT_FOUND
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
    private suspend fun fetchFromDB(): Bitmap?{
        if (imgFetch!=null) {
            return imgFetch!!.fetchImage()
        }
        // Select corresponding fetcher
        val fetcher : ImageFetcher = if (UrlFormat.getUrlType(imgURL) == UrlFormat.URL_IS_FIREBASE) {
            ImageFetcherFirebase(imgURL)
        } else {
            ImageFetcherHttp(imgURL)
        }
        return fetcher.fetchImage()
    }
}