package com.github.palFinderTeam.palfinder.utils.image

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.provider.MediaStore
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.github.palFinderTeam.palfinder.MainActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.chat.ChatActivity
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.navigation.MainNavActivity
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.UIMockProfileServiceModule
import kotlinx.coroutines.test.runTest
import org.junit.Test
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

@HiltAndroidTest
class QRCodeTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)


    @Before
    fun setup() {
        hiltRule.inject()
    }
    @Test
    fun savesExternalWork() = runTest{
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainNavActivity::class.java)

        val scenario = ActivityScenario.launch<MainNavActivity>(intent)
        scenario.onActivity {
            //Initiate the barcode encoder
            val barcodeEncoder = BarcodeEncoder()
            //Encode text in editText into QRCode image into the specified size using barcodeEncoder
            val bitmap = barcodeEncoder.encodeBitmap(
                MEETUP_SHOWN, BarcodeFormat.QR_CODE, it.resources.getInteger(R.integer.QR_size), it.resources.getInteger(
                    R.integer.QR_size))
            val uri = QRCode.saveImageExternal(bitmap, it)
            val decodedUri = ImageDecoder.decodeBitmap(ImageDecoder.createSource(it.contentResolver, uri!!));
            assert(decodedUri.byteCount == bitmap.byteCount)
        }
    }
}