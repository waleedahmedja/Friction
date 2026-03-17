// Root build file — plugin declarations only.
// apply false means plugins are declared here but NOT applied to the root project.
// They get applied in app/build.gradle.kts where they're actually needed.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android)      apply false
    // Kotlin 2.0+ Compose compiler plugin — must be declared here AND in app module
    alias(libs.plugins.kotlin.compose)      apply false
}
