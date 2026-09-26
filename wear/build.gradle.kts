plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "ru.danilp1.remotephoneblocker"
    compileSdk { version = release(37) }

    defaultConfig {
        applicationId = "ru.danilp1.remotephoneblocker"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release { isMinifyEnabled = false }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures { compose = true }
}

dependencies {
    implementation("com.google.android.gms:play-services-wearable:20.0.1")
    implementation("androidx.compose.remote:remote-creation-compose:1.0.0-alpha20")
    implementation("androidx.compose.remote:remote-core:1.0.0-alpha20")
    implementation("androidx.glance.wear:wear:1.0.0-alpha19")
    implementation("androidx.glance.wear:wear-core:1.0.0-alpha19")
    implementation("androidx.wear.compose.remote:remote-material3:1.0.0-alpha12")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}
