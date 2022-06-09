package com.github.palFinderTeam.palfinder.utils.image

import android.app.Activity
import android.content.Intent
import com.github.dhaval2404.imagepicker.ImagePicker

const val IMAGE_WIDTH = 512

/**
 * Start a dialog to choose an image from disk or take a picture of square
 * ratio.
 *
 * @param activity The calling activity.
 * @param onResult Callback when picker finish.
 */
fun pickProfileImage(activity: Activity, onResult: (Intent) -> Unit) {
    ImagePicker.with(activity)
        .cropSquare()
        .maxResultSize(IMAGE_WIDTH, IMAGE_WIDTH)
        .createIntent(onResult)
}