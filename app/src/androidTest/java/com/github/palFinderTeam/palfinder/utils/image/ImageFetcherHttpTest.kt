package com.github.palFinderTeam.palfinder.utils.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

        val bitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565)
        val array = ByteArrayOutputStream()
        Assert.assertNotNull(array)

        Assert.assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 0, array))

        val inputStream: InputStream = ByteArrayInputStream(array.toByteArray())

        val imgUrl = "someUrl"

        val fetcher = ImageFetcherHttp(imgUrl, inputStream)

        val actBitmap = fetcher.fetchImage()
        
    }

}