package com.github.palFinderTeam.palfinder.utils.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.gson.*
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type


class BitmapJsonConverter : JsonSerializer<Bitmap>, JsonDeserializer<Bitmap> {

    companion object {
        const val COMPRESSION_QUALITY = 100
    }
    override fun serialize(
        src: Bitmap?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val encodedImage: String
        val byteArrayBitmapStream = ByteArrayOutputStream()
        src?.compress(
            Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
            byteArrayBitmapStream
        )
        val b: ByteArray = byteArrayBitmapStream.toByteArray()
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT)
        return JsonPrimitive(encodedImage)
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Bitmap {
        val stringPicture = json?.asString
        val decodedString: ByteArray =
        Base64.decode(stringPicture, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }
}