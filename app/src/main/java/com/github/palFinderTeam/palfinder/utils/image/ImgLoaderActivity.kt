package com.github.palFinderTeam.palfinder.utils.image

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.R

class ImgLoaderActivity : AppCompatActivity() {

    private var pic = ImageInstance("icons/cat.png")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_img_display)
    }

    fun loadImg(view: View?) {
        val holder = findViewById<ImageView>(R.id.imgLoaderRes)
        pic.loadImageInto(holder)
    }

}