package com.waleedahmedja.friction

import android.app.Application

/**
 * Application entry point.
 * Lightweight — all initialisation happens lazily in the ViewModel and services.
 */
class FrictionApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
