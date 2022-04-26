package com.github.palFinderTeam.palfinder.cache

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * Cache For Dictionary/Map
 *
 * @param directory: name of the pseudo directory (it is in fact a prefix)
 * @param clazz: Class to store
 * @param permanent: Prevent Android from randomly deleting the data
 */
class DictionaryCache<T> (
    private val directory: String,
    private val clazz: Class<T>,
    private val permanent: Boolean,
    private val context: Context,
    private val policy: ((File) -> Boolean)? = null
){
    companion object{
        fun clearAllTempCaches(context: Context){
            context.cacheDir.listFiles()?.filterNotNull()?.map{ it.delete() }
        }
    }

    init {
        if (policy!=null){
            setEvictPolicy(policy)
        }
    }

    private var wasLoaded = false
    private var hasEvictPolicy = false
    private lateinit var evictPolicy: (File)->Boolean
    var keylist = HashSet<String>()

    private val gson = Gson()

    private fun getDir(): File? {
        return if (permanent){
            context.dataDir
        }
        else{
            context.cacheDir
        }
    }

    /**
     * Return if true if the cache contains the object with key [uuid]
     */
    fun contains(uuid: String, ignorePolicy: Boolean = false): Boolean{
        if (!ignorePolicy) {
            evict()//Optimisation
        }
        val file = File(getDir(), "${directory}_${uuid}")
        return file.exists()
    }

    /**
     * Return if the cached object with key [uuid]
     */
    fun get(uuid: String, ignorePolicy: Boolean = false): T {
        if (!ignorePolicy) {
            evict()//Optimisation
        }
        val file = File(getDir(), "${directory}_${uuid}")
        val content = file.readText()

        return gson.fromJson(JsonParser.parseString(content), clazz)
    }

    /**
     * Store the object [obj] with key [uuid]
     */
    fun store(uuid: String, obj: T, ignorePolicy: Boolean = false){
        if (!ignorePolicy) {
            evict()//Optimisation
        }
        val file = File(getDir(), "${directory}_${uuid}")
        file.writeText(gson.toJson(obj))

        loadMetaCache()
        if (!keylist.contains(uuid)) {
            keylist.add(uuid)
            storeMetaCache()
        }
    }

    /**
     * Delete the cached version of object with key [uuid]
     */
    fun delete(uuid: String, ignorePolicy: Boolean = false){
        if (!ignorePolicy) {
            evict()//Optimisation
        }
        val file = File(getDir(), "${directory}_${uuid}")
        if (file.exists()) {
            file.delete()
        }
        loadMetaCache()
        if (keylist.contains(uuid)) {
            keylist.remove(uuid)
            storeMetaCache()
        }
    }

    /**
     * Return all cached objects as a list
     */
    fun getAll():List<T>{
        loadMetaCache()
        return keylist.filter { contains(it) }.map { get(it) }.toList()
    }
    fun evict(){
        if (hasEvictPolicy){
            keylist.filter { contains(it, true) }
                .filter { evictPolicy(File(getDir(), "${directory}_${it}")) }
                .map{ delete(it, true) }
        }
    }
    fun setEvictPolicy(policy: (File)->Boolean){
        evictPolicy = policy
        hasEvictPolicy = true
    }

    private fun storeMetaCache(){
        val file = File(getDir(), directory)
        file.writeText(gson.toJson(keylist))
    }
    private fun loadMetaCache(){
        if (!wasLoaded) {
            wasLoaded = true
            val file = File(getDir(), directory)
            if (file.exists()) {
                val content = file.readText()

                val itemType = object : TypeToken<HashSet<String>>() {}.type
                keylist = gson.fromJson(JsonParser.parseString(content), itemType)
            }
        }
    }
}