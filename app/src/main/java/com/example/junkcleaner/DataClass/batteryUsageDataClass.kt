package com.example.junkcleaner.DataClass

import android.graphics.drawable.Drawable

data class batteryUsageDataClass(
    val name:String,
    val icon:Drawable,
    val packageName:String,
    val time:String,
    val grade:String
) {
}