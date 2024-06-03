package com.example.junkcleaner

interface BluetoothStatusListener {
    fun onbluetoothStatusChanged(isEnabled: Boolean)
}