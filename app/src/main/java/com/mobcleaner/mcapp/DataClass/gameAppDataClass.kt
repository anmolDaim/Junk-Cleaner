package com.mobcleaner.mcapp.DataClass

import android.graphics.drawable.Drawable

data class gameAppDataClass(
    val appName:String,
    val appIcon:Drawable,
    val installDate:String,
    val gameSize:String,
    val packageName:String
) {
}