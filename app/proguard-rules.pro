# Friction — ProGuard rules
# The default proguard-android-optimize.txt handles most cases.
# Add project-specific rules here if needed.

# Keep BiometricPrompt callback
-keep class androidx.biometric.** { *; }

# Keep Device Admin receiver
-keep class com.waleedahmedja.friction.admin.** { *; }

# Keep Accessibility Service
-keep class com.waleedahmedja.friction.service.** { *; }
