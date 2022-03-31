package com.github.palFinderTeam.palfinder.utils.images

import android.graphics.Bitmap
import com.github.palFinderTeam.palfinder.utils.image.ImageFetcherHttp
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ImageFetcherHttpTest {

    @Test
    fun testFetchImage() = runTest{

        val bitmap: Bitmap = Bitmap.createBitmap(5, 5, Bitmap.Config.RGB_565)
        val array = ByteArrayOutputStream()
        Assert.assertNotNull(array)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, array)

        val inputStream: InputStream = ByteArrayInputStream(array.toByteArray())

        val imgUrl = "https://someUrl.test"

        val fetcher = ImageFetcherHttp(imgUrl, inputStream)

        val actBitmap = fetcher.fetchImage()

        Assert.assertTrue(bitmap.sameAs(actBitmap))

    }

}