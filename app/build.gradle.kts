plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization) // Added for kotlinx.serialization
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.shadow.hellotv"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.shadow.hellotv"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json) // or latest version
    implementation(libs.material3)
    implementation(libs.kotlinx.coroutines.android)
// https://mvnrepository.com/artifact/androidx.media3/media3-exoplayer
    implementation(libs.androidx.media3.exoplayer)
// https://mvnrepository.com/artifact/androidx.media3/media3-ui
    implementation(libs.androidx.media3.ui)
// https://mvnrepository.com/artifact/androidx.media3/media3-exoplayer-dash
    implementation(libs.androidx.media3.exoplayer.dash)
// https://mvnrepository.com/artifact/androidx.media3/media3-exoplayer-hls
    implementation(libs.androidx.media3.exoplayer.hls)
    // https://mvnrepository.com/artifact/androidx.media3/media3-datasource-rtmp
    implementation(libs.androidx.media3.datasource.rtmp)
    // https://mvnrepository.com/artifact/io.coil-kt.coil3/coil
    implementation(libs.coil.compose)

    // material for tv
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)

    // icon
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}