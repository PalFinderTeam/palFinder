package com.github.palFinderTeam.palfinder.cache

import android.content.Context
import android.graphics.Bitmap
import com.github.palFinderTeam.palfinder.utils.image.BitmapJsonConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import java.io.File

/**
 * Cache for a simple unique object.
 *
 * @param name: name of the file
 * @param clazz: Class to store
 * @param permanent: Prevent Android from randomly deleting the data
 * @param contextService: Context Provider
 */
class FileCache<T> (
    private val name: String,
    private val clazz: Class<T>,
    private val permanent: Boolean,
    private val context: Context,
){

    private val bitmapJsonConverter = BitmapJsonConverter()
    private val gson = GsonBuilder().registerTypeAdapter(Bitmap::class.java, bitmapJsonConverter).create()

    private fun getDir(): File? {
        return if (permanent){
            context.dataDir
        }
        else{
            context.cacheDir
        }
    }

    /**
     * Return if true if the cache contains the object
     */
    fun exist(): Boolean{
        val file = File(getDir(), name)
        return file.exists()
    }

    /**
     * Return if the cached object
     */
    fun get(): T {
        val file = File(getDir(), name)
        val content = file.readText()

        return gson.fromJson(JsonParser.parseString(content), clazz)
    }

    /**
     * Store the object [obj]
     */
    fun store(obj: T){
        val file = File(getDir(), name)
        file.writeText(gson.toJson(obj))
    }

    /**
     * Delete the cached version of object
     */
    fun delete(){
        val file = File(getDir(), name)
        if (file.exists()) {
            file.delete()
        }
    }
}