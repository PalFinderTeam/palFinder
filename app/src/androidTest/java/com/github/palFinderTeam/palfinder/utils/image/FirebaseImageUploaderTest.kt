package com.github.palFinderTeam.palfinder.utils.image

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import androidx.test.platform.app.InstrumentationRegistry
import com.github.palFinderTeam.palfinder.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

const val ONE_MB: Long = 1024 * 1024
@ExperimentalCoroutinesApi
class FirebaseImageUploaderTest {

    private lateinit var storage: FirebaseStorage
    private lateinit var imageUploader: FirebaseImageUploader
    private lateinit var imageUri: Uri

    @Before
    fun setUp() {
        // Connect to local emulator
        FirebaseStorage.getInstance().useEmulator("10.0.2.2", 9199)
        storage = FirebaseStorage.getInstance()
        imageUploader = FirebaseImageUploader(storage)

        imageUri = Uri.parse("android.resource://com.github.palFinderTeam.palfinder/" + R.drawable.demo_pfp)
    }

/*
    @Test
    fun uploadImageUploadInDb() = runTest {
        val id = imageUploader.uploadImage(imageUri)
        assertThat(id, `is`(notNullValue()))
        id?.let { id ->
            val docInDb = storage.reference.child(id)

            val downloadedImg = docInDb.getBytes(ONE_MB * 50).await()
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val localImageBytes = context.contentResolver.openInputStream(imageUri)?.buffered()?.use { it.readBytes() }
            assertThat(localImageBytes, `is`(notNullValue()))
            localImageBytes?.let { bytes ->
                assertThat(downloadedImg, `is`(bytes))
            }

        }
    }
*/
}