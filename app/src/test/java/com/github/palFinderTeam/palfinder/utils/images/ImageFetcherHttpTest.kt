package com.github.palFinderTeam.palfinder.utils.images

import android.graphics.BitmapFactory
import com.github.palFinderTeam.palfinder.utils.image.ImageFetcherHttp
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ImageFetcherHttpTest {

    @Test
    fun testFetchImage() = runTest{
        val mockFetcher = Mockito.mock(ImageFetcherHttp::class.java)
        val imgUrl = "https://test.url"
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)

        val bitmap = Mockito.`when`(mockFetcher.fetchImage()).then {

            val url = URL(mockFetcher.imgURL)
            Assert.assertEquals(imgUrl, url.readText())

            var connection: HttpURLConnection?

            connection = url.openConnection() as HttpURLConnection
            Assert.assertEquals(url, connection.url)

            connection = mockConnection

            val inputStream: InputStream = connection.inputStream
            val bufferedInputStream = BufferedInputStream(inputStream)
            BitmapFactory.decodeStream(bufferedInputStream)
        }


    }

}