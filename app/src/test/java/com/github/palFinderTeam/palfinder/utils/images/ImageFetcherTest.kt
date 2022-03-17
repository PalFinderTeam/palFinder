package com.github.palFinderTeam.palfinder.utils.images
/*
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import androidx.core.graphics.get
import com.github.palFinderTeam.palfinder.utils.image.ImageFetcher
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito


class ImageFetcherTest {
    lateinit var imgFetch : ImageFetcher
    lateinit var imageView : ImageView
    lateinit var bm_ref : Bitmap
    lateinit var bm_load_val : Integer
    val BM_REF_VAL = 5
    var bm_val = 0

    @Before
    fun init(){
        imageView = Mockito.mock(ImageView::class.java)
        Mockito.`when`(imageView.setImageBitmap(Mockito.any(Bitmap::class.java))).thenAnswer {
            bm_val = it.getArgument<Bitmap>(0).get(0,0)
            true
        }

        bm_ref = Mockito.mock(Bitmap::class.java)
        Mockito.`when`(bm_ref.get(Mockito.any(Int::class.java), Mockito.any(Int::class.java))).thenAnswer { BM_REF_VAL }

        imgFetch = object : ImageFetcher {
            override suspend fun fetchImage(): Bitmap? {
                return bm_ref
            }
        }
    }

    @Test
    fun imageInstanceInject() = runTest {
        init()
        ImageInstance("ah", imgFetch).loadImageInto(imageView)

        //Assert.assertEquals(BM_REF_VAL, bm_val)
    }

}*/
