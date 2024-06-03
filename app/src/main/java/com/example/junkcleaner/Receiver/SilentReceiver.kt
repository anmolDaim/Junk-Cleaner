package com.example.junkcleaner.Receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.getSystemService
import com.example.junkcleaner.R
import com.example.junkcleaner.SilentStatusListener
import com.example.junkcleaner.wifiStatusListener

class SilentReceiver(private val listener: SilentStatusListener):BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == AudioManager.RINGER_MODE_CHANGED_ACTION) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val ringerMode = audioManager.getRingerMode()

            listener.onSilentStatusChanged(ringerMode == AudioManager.RINGER_MODE_SILENT)

        }
    }
}