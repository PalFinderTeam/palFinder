package com.github.palFinderTeam.palfinder.utils.image

import android.graphics.Bitmap
import com.github.palFinderTeam.palfinder.utils.image.ImageFetcherHttp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

@ExperimentalCoroutinesApi
class ImageFetcherHttpTest {

    @Test
    fun testFetchImage() = runTest{

        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val bitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565)
        val array = ByteArrayOutputStream()

        assertThat(array, `is`(notNullValue()))
        assertThat(bitmap.compress(Bitmap.CompressFormat.PNG, 0, array), `is`(true))

        val inputStream: InputStream = ByteArrayInputStream(array.toByteArray())

        val imgUrl = "someUrl"

        val fetcher = ImageFetcherHttp(imgUrl, inputStream, dispatcher)

        val actBitmap = fetcher.fetchImage()

    }

}