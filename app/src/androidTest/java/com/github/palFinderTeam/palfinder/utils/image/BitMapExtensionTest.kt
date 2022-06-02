package com.github.palFinderTeam.palfinder.utils.image

import android.graphics.*
import com.github.palFinderTeam.palfinder.utils.image.addBorder
import org.junit.Assert
import org.junit.Test

class BitMapExtensionTest {

    @Test
    fun addBorderTest(){
        val width = 16
        val height = 16
        val color = Color.RED
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val borderBitMap = bitmap.addBorder(color)
        val borderColor = borderBitMap?.getColor(width/2, 0)
        Assert.assertEquals(width, borderBitMap?.width)
        Assert.assertEquals(height, borderBitMap?.height)
        Assert.assertEquals(Color.valueOf(color).red(), borderColor?.red())
        Assert.assertEquals(Color.valueOf(color).green(), borderColor?.green())
        Assert.assertEquals(Color.valueOf(color).blue(), borderColor?.blue())
    }

}