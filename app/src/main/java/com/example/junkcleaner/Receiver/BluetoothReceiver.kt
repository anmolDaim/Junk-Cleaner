package com.example.junkcleaner.Receiver

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.junkcleaner.BluetoothStatusListener
import com.example.junkcleaner.MobileDataStatusListener
import com.example.junkcleaner.R

class BluetoothReceiver(private val listener: BluetoothStatusListener):BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val bluetoothEnabled = bluetoothAdapter.isEnabled

            listener.onbluetoothStatusChanged(bluetoothEnabled == true)

        }
    }
}