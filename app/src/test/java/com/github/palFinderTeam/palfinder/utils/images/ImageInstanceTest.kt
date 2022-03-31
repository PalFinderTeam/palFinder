package com.github.palFinderTeam.palfinder.utils.images

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class ImageInstanceTest {

    private lateinit var imgUrl: String
    private lateinit var imageInstance: ImageInstance
    private val fetcher = MockImageFetcher()

    private var fetcherBitmap: Bitmap = Mockito.mock(Bitmap::class.java)

    private var mockView = Mockito.mock(ImageView::class.java)
    private var imgResId = 0
    private var viewBitmap: Bitmap? = null

    @Before
    fun init() = runTest{
        imgUrl = "someUrl"
        fetcher.bitmap = fetcherBitmap
        imageInstance = ImageInstance(imgUrl, fetcher)
        viewBitmap = null


        Mockito.`when`(mockView.setImageBitmap(Mockito.any(Bitmap::class.java))).then {
            viewBitmap = it.getArgument(0)
            Any()
        }
        Mockito.`when`(mockView.setImageResource(Mockito.any(Int::class.java))).then{
            imgResId = it.getArgument(0)
            Any()
        }
    }

    @Test
    fun loadEmptyString() = runTest{
        imgUrl = ""
        imageInstance = ImageInstance(imgUrl, fetcher)
        imageInstance.loadImageInto(mockView)
        Assert.assertEquals(null, viewBitmap)
        Assert.assertEquals(R.drawable.not_found, imgResId)
        Assert.assertEquals(ImageInstance.FILE_NOT_FOUND, imageInstance.imgStatus)
        Assert.assertEquals(false, imageInstance.isCached)
    }

    @Test
    fun loadImageTest() = runTest{
        imageInstance.loadImageInto(mockView)
        Assert.assertEquals(fetcherBitmap, viewBitmap)
        Assert.assertEquals(android.R.color.background_dark, imgResId)
        Assert.assertEquals(true, imageInstance.isCached)
        Assert.assertEquals(ImageInstance.CACHED, imageInstance.imgStatus)
    }

    @Test
    fun testCache() = runTest{
        imageInstance.loadImageInto(mockView)
        mockView.setImageBitmap(null)
        //only to assert it wasn't modified
        imageInstance.imgStatus = ImageInstance.NOT_LOADED
        imageInstance.loadImageInto(mockView)
        Assert.assertEquals(fetcherBitmap, viewBitmap)
        Assert.assertEquals(ImageInstance.NOT_LOADED, imageInstance.imgStatus)

    }

    @Test
    fun clearImageCacheTest() = runTest{
        imageInstance.loadImageInto(mockView)
        Assert.assertEquals(true, imageInstance.isCached)
        imageInstance.isCached = false
        imageInstance.clearImageCache()
        Assert.assertEquals(ImageInstance.CACHED, imageInstance.imgStatus)
        imageInstance.isCached = true
        imageInstance.clearImageCache()
        Assert.assertEquals(ImageInstance.NOT_LOADED, imageInstance.imgStatus)
        Assert.assertEquals(false, imageInstance.isCached)

    }
}