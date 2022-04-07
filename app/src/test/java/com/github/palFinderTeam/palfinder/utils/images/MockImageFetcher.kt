package com.github.palFinderTeam.palfinder.utils.images

import android.graphics.Bitmap
import com.github.palFinderTeam.palfinder.utils.image.ImageFetcher

class MockImageFetcher : ImageFetcher{

    var bitmap: Bitmap? = null

    override suspend fun fetchImage(): Bitmap? {
        return bitmap
    }

}