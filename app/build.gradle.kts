plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.projectscanotes"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.projectscanotes"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Tambahan konfigurasi untuk memperbaiki error "16 KB devices compatible" pada ML Kit
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    // AndroidX & Material UI Base
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // ML Kit Text Recognition (OCR)
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // CameraX Core & View (Disamakan ke versi stabil terbaru)
    val cameraXVersion = "1.3.4"
    implementation("androidx.camera:camera-camera2:$cameraXVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraXVersion")
    implementation("androidx.camera:camera-view:$cameraXVersion")

    // Library Volley untuk koneksi internet ke Google Gemini AI
    implementation("com.android.volley:volley:1.2.1")

    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}