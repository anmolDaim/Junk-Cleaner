package com.mobcleaner.app.DataClass

import android.graphics.drawable.Drawable

data class networkTrafficDataClass(
    val appName: String,
    val dataUsage: String,
    val icon: Drawable,
    val mobileDataUsage: Int,
    val wifiDataUsage: Int,
    val packageName: String
    ) {
}