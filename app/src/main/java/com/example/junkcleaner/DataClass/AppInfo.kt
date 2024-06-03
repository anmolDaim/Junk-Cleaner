package com.example.junkcleaner.DataClass

import android.graphics.drawable.Drawable

data class AppInfo(
    val appName:String,
    val appImage: Drawable,
    val lastUsed:Long
) {
}