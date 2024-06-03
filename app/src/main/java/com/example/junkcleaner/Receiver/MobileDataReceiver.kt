package com.example.junkcleaner.Receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.example.junkcleaner.MobileDataStatusListener
import com.example.junkcleaner.R

class MobileDataReceiver(private val listener: MobileDataStatusListener) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mobileDataEnabled = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)?.isConnected

            listener.onMobileDataStatusChanged(mobileDataEnabled == true)
        }
    }
}