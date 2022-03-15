package com.github.palFinderTeam.palfinder.utils.image

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.R
import java.lang.Exception

class ImgLoaderActivity : AppCompatActivity() {

    private var pic2 = ImageInstance("https://raw.githubusercontent.com/PalFinderTeam/palFinder/master/app/src/main/res/drawable/demo_pfp.jpeg")
    private var pic1 = ImageInstance("icons/cat.png")
    private var pic_fail = ImageInstance("nooooo")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_img_display)
    }

    fun loadImg1(view: View?) {
        val holder = findViewById<ImageView>(R.id.imgLoaderRes)
        try {
            pic1.loadImageInto(holder)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG)
        }

    }

    fun loadImg2(view: View?) {
        val holder = findViewById<ImageView>(R.id.imgLoaderRes)
        try {
            pic2.loadImageInto(holder)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG)
        }
    }

    fun loadImgFail(view: View?) {
        val holder = findViewById<ImageView>(R.id.imgLoaderRes)
        try {
            pic_fail.loadImageInto(holder)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG)
        }
    }

    fun clearCache(view: View?) {
        pic1.clearImageCache()
        pic2.clearImageCache()
        pic_fail.clearImageCache()
    }

}