package com.ubaydullayev.expensetracker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

/**
 * Application entry point.
 *
 * The whole UI is designed for a light surface only (no dark palette exists yet). We pin the app to
 * [AppCompatDelegate.MODE_NIGHT_NO] so that neither the system Dark Mode setting nor an OEM
 * "auto dark" feature (e.g. Samsung) swaps in night resources or inverts our colors on device.
 * This is the runtime counterpart to the Light-only theme and `android:forceDarkAllowed=false`.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
