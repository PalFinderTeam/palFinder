package com.github.palFinderTeam.palfinder.utils.image

import android.graphics.Bitmap
import android.util.Base64
import com.github.palFinderTeam.palfinder.utils.image.BitmapJsonConverter.Companion.COMPRESSION_QUALITY
import com.google.gson.JsonPrimitive
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.ByteArrayOutputStream

class BitmapJsonConverterTest {

    @Test
    fun serializeGiveRightResult() {
        val bitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565)
        val array = ByteArrayOutputStream()

        assertThat(array, `is`(notNullValue()))
        assertThat(
            bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, array),
            `is`(true)
        )

        val serialized = BitmapJsonConverter().serialize(bitmap, null, null)
        assertThat(serialized, `is`(instanceOf(JsonPrimitive::class.java)))
        val content = (serialized as JsonPrimitive).asString
        assertThat(Base64.encodeToString(array.toByteArray(), Base64.DEFAULT), `is`(content))
    }

    @Test
    fun deserializeGiveRightResult() {
        val bitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565)
        val array = ByteArrayOutputStream()

        assertThat(array, `is`(notNullValue()))
        assertThat(
            bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, array),
            `is`(true)
        )

        val serialized = BitmapJsonConverter().serialize(bitmap, null, null)
        assertThat(serialized, `is`(instanceOf(JsonPrimitive::class.java)))
        val content = BitmapJsonConverter().deserialize(serialized, null, null)
    }
}