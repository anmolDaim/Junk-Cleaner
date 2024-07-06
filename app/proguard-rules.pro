# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


-keep class com.mobcleaner.mcapp.Activity.*{ *; }
-keep class com.mobcleaner.mcapp.Fragment.* { *; }
-keep class com.mobcleaner.mcapp.Adapter.* { *; }
-keep class com.mobcleaner.mcapp.DataClass.* { *; }
-keep class com.mobcleaner.mcapp.transition.* { *; }
-keep class com.mobcleaner.mcapp.entity.* { *; }
-keep class com.mobcleaner.mcapp.service.* { *; }
-keep class com.mobcleaner.mcapp.util.* { *; }

#PhilJay:MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }


#admob
-keep class com.google.ads.mediation.admob.AdMobAdapter {
    *;
}

-keep class com.google.ads.mediation.AdUrlAdapter {
    *;
}

#Antivirus SDK
-keep class com.trustlook.** {
*;
}


