package com.github.palFinderTeam.palfinder.easter_egg

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.github.palFinderTeam.palfinder.R


class ManeameaFragment : DialogFragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.activity_video, container, false)
        val videoView = v.findViewById<VideoView>(R.id.easter_egg_video)
        val uri = Uri.parse("android.resource://" + requireActivity().packageName + "/" + R.raw.maneamea)
        videoView.setVideoURI(uri)
        //videoView.scaleX = 2.0F
        //videoView.scaleY = 2.0F
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