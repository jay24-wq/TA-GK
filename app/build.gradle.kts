plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.kelompok.bouncingball"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.kelompok.bouncingball"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    testImplementation(libs.junit)
}
