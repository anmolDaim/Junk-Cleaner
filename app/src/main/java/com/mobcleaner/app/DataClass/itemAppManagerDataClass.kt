package com.mobcleaner.app.DataClass

import android.graphics.drawable.Drawable

data class itemAppManagerDataClass(
    val appName:String?,
    val appImage:Drawable?,
    val packageName:String?,
    val checkboxVisibility: Int?,
    val appSize:String?,
    val sizeCheckboxVisibility: Int?,
    val appInstall:String?,
    val installCheckboxVisibility: Int?,
    var isSelected: Boolean = false
) {
}