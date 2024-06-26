plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.mobcleaner.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mobcleaner.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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

}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.activity:activity:1.9.0")
    //implementation("androidx.activity:activity:1.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //lottlie
    implementation("com.airbnb.android:lottie:4.2.2")
//graph view line chart
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    //graphview bar chart
    implementation ("com.jjoe64:graphview:4.2.2")
//speedometer
    implementation("com.github.ibrahimsn98:speedometer:1.0.1")
//okhttp
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    //coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
//broadcast receiver
    implementation ("androidx.localbroadcastmanager:localbroadcastmanager:1.0.0")
    //ads 20.5.0
    implementation ("com.google.android.gms:play-services-ads:23.1.0")
    //native_ads
    implementation ("androidx.work:work-runtime:2.7.0")
    // Add Firebase Crashlytics dependency
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
}

