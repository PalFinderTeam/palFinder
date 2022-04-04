package com.github.palFinderTeam.palfinder.cache

import android.content.Context
import com.github.palFinderTeam.palfinder.PalFinderApplication
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.io.File


class DictionaryCache<T> {
    fun contains(uuid: String): Boolean{
        val file = File.createTempFile(uuid, null, getContext().cacheDir)
        return file.exists()
    }
    fun get(uuid: String): T {
        val file = File.createTempFile(uuid, null, getContext().cacheDir)
        val content = file.readText()

        val gson = Gson()
        return gson.fromJson(JsonParser.parseString(content), Object::class.java) as T
    }
    fun store(uuid: String, obj: T){

    }
    fun getContext(): Context {
        return PalFinderApplication.instance.applicationContext
    }
}