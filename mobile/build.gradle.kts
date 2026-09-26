plugins {
    id("com.android.application")
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
}

dependencies {
    implementation("com.google.android.gms:play-services-wearable:20.0.1")
}
