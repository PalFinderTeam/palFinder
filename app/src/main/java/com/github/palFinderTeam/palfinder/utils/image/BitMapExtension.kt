package com.github.palFinderTeam.palfinder.utils.image

import android.graphics.*


/**
 * create a circle border to a bitmap
 * @param borderWidth width of the border, default is 5
 * @param borderColor color of the border, default is RED
 *
 * code is taken from : https://www.android--code.com/2020/06/android-kotlin-circular-bitmap-with.html
 */
fun Bitmap.addBorder(borderWidth: Int = 5, borderColor: Int = Color.RED): Bitmap?{
    val bitmap = Bitmap.createBitmap(this.width + width*2, this.height+width*2, Bitmap.Config.ARGB_8888)

    // canvas to draw circular bitmap
    val canvas = Canvas(bitmap)

    // get the maximum radius
    val radius = (width / 2f).coerceAtMost(height / 2f)

    // create a path to draw circular bitmap border
    val borderPath = Path().apply {
        addCircle(
            width/2f,
            height/2f,
            radius,
            Path.Direction.CCW
        )
    }

    // draw border on circular bitmap
    canvas.clipPath(borderPath)
    canvas.drawColor(borderColor)


    // create a path for circular bitmap
    val bitmapPath = Path().apply {
        addCircle(
            width/2f,
            height/2f,
            radius - borderWidth,
            Path.Direction.CCW
        )
    }

    canvas.clipPath(bitmapPath)
    val paint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        isAntiAlias = true
    }

    // clear the circular bitmap drawing area
    // it will keep bitmap transparency
    canvas.drawBitmap(this,0f,0f,paint)

    // now draw the circular bitmap
    canvas.drawBitmap(this,0f,0f,null)


    val diameter = (radius*2).toInt()
    val x = (width - diameter)/2
    val y = (height - diameter)/2

    // return cropped circular bitmap with border
    return Bitmap.createBitmap(
        bitmap, // source bitmap
        x, // x coordinate of the first pixel in source
        y, // y coordinate of the first pixel in source
        diameter, // width
        diameter // height
    )
}

