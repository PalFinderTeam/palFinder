package com.github.palFinderTeam.palfinder.utils.image

import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FileDownloadTask

/**
 * `ImageFetcher` is able to fetch an image from the internet
 */
interface ImageFetcher {

    companion object { const val TMP_NAME : String = "tmp_image" }

    /**
     * Get the bitmap from the given URL, async
     */
    suspend fun fetchImage() : Bitmap?
}