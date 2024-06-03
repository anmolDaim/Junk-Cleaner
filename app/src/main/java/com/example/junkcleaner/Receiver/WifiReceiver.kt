package com.example.junkcleaner.Receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import com.example.junkcleaner.MobileDataStatusListener
import com.example.junkcleaner.R
import com.example.junkcleaner.wifiStatusListener

class WifiReceiver(private val listener: wifiStatusListener):BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiEnabled = wifiManager.isWifiEnabled

            listener.onWifiStatusChanged(wifiEnabled == true)

        }
    }
}