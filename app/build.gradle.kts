// ─────────────────────────────────────────────────────────────────────────────
// Friction — app/build.gradle.kts
// AGP 8.7.3 · Kotlin 2.0.21 · compileSdk 35 · minSdk 24
// ─────────────────────────────────────────────────────────────────────────────

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Kotlin 2.0 requires this explicit Compose compiler plugin.
    // Do NOT use the old composeOptions { kotlinCompilerExtensionVersion } block.
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace  = "com.waleedahmedja.friction"
    compileSdk = 35

    defaultConfig {
        applicationId             = "com.waleedahmedja.friction"
        minSdk                    = 24
        targetSdk                 = 35
        versionCode               = 1
        versionName               = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Minify + resource shrink for a tight production APK
            isMinifyEnabled   = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
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
        compose     = true
        // Enables BuildConfig.VERSION_NAME used in AboutScreen
        buildConfig = true
    }
}

dependencies {

    // ── Android core ──────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)

    // AppCompat is REQUIRED here because themes.xml uses Theme.AppCompat.NoActionBar.
    // Without this, the app crashes with a resource-not-found at launch.
    implementation(libs.androidx.appcompat)

    // ── Compose BOM — all compose/* versions come from here ───────────────────
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // ── Navigation ────────────────────────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)

    // ── DataStore ─────────────────────────────────────────────────────────────
    implementation(libs.androidx.datastore.preferences)

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.vm.compose)
    implementation(libs.androidx.lifecycle.rt.compose)

    // ── Activity ──────────────────────────────────────────────────────────────
    implementation(libs.androidx.activity.compose)

    // ── Biometric ─────────────────────────────────────────────────────────────
    implementation(libs.androidx.biometric)

    // ── Tests ─────────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
