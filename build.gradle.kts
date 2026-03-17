// Root-level build file.
// Module-level build config lives in app/build.gradle.kts.
// We declare plugins here with apply false so the version catalog controls
// all versions from a single place (gradle/libs.versions.toml).

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose)      apply false
    alias(libs.plugins.kotlin.android)      apply false
}
