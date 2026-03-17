// ─────────────────────────────────────────────────────────────────────────────
// Friction — app/build.gradle.kts
//
// AGP 8.7.3 · Kotlin 2.0.21 · Compose BOM 2024.12.01
// compileSdk / targetSdk = 35  ·  minSdk = 24 (covers ~97% of Android devices)
// ─────────────────────────────────────────────────────────────────────────────

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Kotlin 2.0+ requires the Compose compiler plugin declared EXPLICITLY here.
    // Do NOT use composeOptions{} block — that's the old Kotlin 1.x way.
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace   = "com.waleedahmedja.friction"
    compileSdk  = 35

    defaultConfig {
        applicationId          = "com.waleedahmedja.friction"
        minSdk                 = 24
        targetSdk              = 35
        versionCode            = 1
        versionName            = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Enable minification for release — removes dead code, shrinks APK
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Keep debug builds fast and unobfuscated
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
        buildConfig = true   // needed if we read BuildConfig.VERSION_NAME in AboutScreen
    }
}

dependencies {

    // ── Core Android ──────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)

    // AppCompat — REQUIRED because themes.xml uses Theme.AppCompat.NoActionBar.
    // Material3 theme parent causes AAPT crash on many device/AGP combinations.
    implementation(libs.androidx.appcompat)

    // ── Compose BOM (pins all Compose library versions together) ─────────────
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

    // ── DataStore (replaces SharedPreferences — coroutine-safe, type-safe) ───
    implementation(libs.androidx.datastore.preferences)

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.vm.compose)
    implementation(libs.androidx.lifecycle.rt.compose)

    // ── Activity ──────────────────────────────────────────────────────────────
    implementation(libs.androidx.activity.compose)

    // ── Biometric (face/fingerprint gate before tap challenge) ────────────────
    implementation(libs.androidx.biometric)

    // ── Testing ───────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
