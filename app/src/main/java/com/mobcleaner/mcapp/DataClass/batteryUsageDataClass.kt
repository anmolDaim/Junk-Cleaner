package com.mobcleaner.mcapp.DataClass

import android.graphics.drawable.Drawable

data class batteryUsageDataClass(
    val name:String,
    val icon:Drawable,
    val packageName:String,
    val time:String,
    val grade:String
) {
}