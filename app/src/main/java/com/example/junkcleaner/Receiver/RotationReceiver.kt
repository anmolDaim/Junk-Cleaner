package com.example.junkcleaner.Receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.junkcleaner.MobileDataStatusListener
import com.example.junkcleaner.R
import com.example.junkcleaner.RotationStatusListener

class RotationReceiver(private val listener: RotationStatusListener):BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Settings.ACTION_DISPLAY_SETTINGS) {
            val rotationEnabled = Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1

            listener.onRotationStatusChanged(rotationEnabled == true)

        }
    }
}