package com.github.palFinderTeam.palfinder.utils.images

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import com.github.palFinderTeam.palfinder.utils.image.ImageFetcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test


class ImageFetcherTest {

    @Test
    fun imageFetcherInterfaceUsage() = runTest {
        val imgFetch = object : ImageFetcher {
            override suspend fun fetchImage(): Bitmap? {
                return null
            }
        }
        Assert.assertEquals(null, imgFetch.fetchImage())
    }
}
