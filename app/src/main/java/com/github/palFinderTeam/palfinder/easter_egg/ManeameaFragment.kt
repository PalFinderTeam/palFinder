package com.github.palFinderTeam.palfinder.easter_egg

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.VideoView
import androidx.fragment.app.DialogFragment
import com.github.palFinderTeam.palfinder.R

/**
 * easter egg fragment, simply plays a video
 */
class ManeameaFragment : DialogFragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.activity_video, container, false)
        val videoView = v.findViewById<VideoView>(R.id.easter_egg_video)
        val uri = Uri.parse("https://i.imgur.com/Daoab5M.mp4")
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { videoView.start() }
        videoView.setOnCompletionListener {
            dialog?.dismiss()
        }
        return v
    }

    override fun onStart() {
        super.onStart()
        // Force the dialog to take whole width
        dialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }
}