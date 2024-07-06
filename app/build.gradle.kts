plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.mobcleaner.mcapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mobcleaner.mcapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 5
        versionName = "1.4"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("debug")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/notice.txt")
        exclude("META-INF/ASL2.0")
    }

}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.activity:activity:1.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.0")

    // lottie
    implementation("com.airbnb.android:lottie:6.4.1")
    // graph view line chart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    // graphview bar chart
    implementation("com.jjoe64:graphview:4.2.2")
    // speedometer
    implementation("com.github.ibrahimsn98:speedometer:1.0.1")
    // okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // broadcast receiver
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    // ads
    implementation("com.google.android.gms:play-services-ads:23.1.0")
    // native_ads
    implementation("androidx.work:work-runtime:2.9.0")
    // Add Firebase Crashlytics dependency
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    // speedometer
    implementation("com.github.anastr:speedviewlib:1.6.1")
    // chrome tab
    implementation("androidx.browser:browser:1.8.0")

    // Antivirus SDK
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(files("E:\\Android projects\\junk cleaner\\trustlook_cloudscan_sdk\\trustlook_cloudscan_sdk_lite_5.0.5.20240619\\DemoProject\\DemoApp\\libs\\cloudscan_sdk_5.0.5.20240619.aar"))
    implementation(files("E:\\Android projects\\junk cleaner\\trustlook_cloudscan_sdk\\trustlook_cloudscan_sdk_lite_5.0.5.20240619\\DemoProject\\DemoApp\\libs\\cleanjunk_sdk_3.0.4.20230109.aar"))

}

