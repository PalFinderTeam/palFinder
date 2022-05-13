package com.github.palFinderTeam.palfinder.utils.image

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


object BitmapSave {
    /**
     * Saves the image as PNG to the app's private external storage folder.
     * @param image Bitmap to save.
     * @return Uri of the saved file or null
     */
    private fun saveImageExternal(image: Bitmap): Uri? {
        //TODO - Should be processed in another thread
        var uri: Uri? = null
        try {
            val file = File("to-share.png")
            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.close()
            uri = Uri.fromFile(file)
        } catch (e: IOException) {
            Log.d(TAG, "IOException while trying to write file for sharing: " + e.message)
        }
        return uri
    }

    /**
     * Checks if the external storage is writable.
     * @return true if storage is writable, false otherwise
     */
    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

    /**
     * Shares the PNG image from Uri.
     * @param uri Uri of image to share.
     */
    fun shareImageUri(image: Bitmap): Intent {
        return if (isExternalStorageWritable()) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_STREAM, saveImageExternal(image))
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.type = "image/png"
            intent
        } else {
            Intent()
        }
    }
}