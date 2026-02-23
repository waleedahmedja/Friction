-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences$Key { *; }
-keep class com.waleedahmedja.friction.viewmodel.** { *; }
-keep class com.waleedahmedja.friction.viewmodel.LockState { *; }
-keep class com.waleedahmedja.friction.viewmodel.TapState { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}