package com.github.palFinderTeam.palfinder.utils.image

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.github.palFinderTeam.palfinder.R
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.Serializable


data class ImageInstance(
    val imgFirebasePath : String
) : Serializable{

    private lateinit var localFile : File
    var isInit : Boolean = false

    /**
     * Loads the image asynchronously into the desired Image View
     * @param view ImageView that needs to be change
     */
    fun loadImageInto(view : ImageView){
        println("Loading image")
        if (isInit) {
            println("Image is already existing!")
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            view.setImageBitmap(bitmap)
        } else {
            println("Fetching from Database")
            runBlocking {
                launch { fetchFromDB(view) }
            }
        }
    }

    private fun fetchFromDB(view : ImageView){
        // Create loading effect
        view.setImageResource(android.R.color.background_dark)
        view.alpha = 0.3f

        // References for the image
        val storageRef = FirebaseStorage.getInstance().reference.child(imgFirebasePath)
        localFile = File.createTempFile("tmpImg", "png")

        // Try download image
        storageRef.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            view.setImageBitmap(bitmap)
            isInit = true
        }.addOnFailureListener{
            println("Failed to load image")
            view.setImageResource(R.drawable.not_found)
        }
        view.alpha = 1f
    }
}