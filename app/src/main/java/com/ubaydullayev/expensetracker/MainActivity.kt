package com.ubaydullayev.expensetracker

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1) OS splash during cold start (no color flash). Must precede super.onCreate().
        installSplashScreen()

        // 2) True edge-to-edge: makes the status & navigation bars transparent, tells the window to
        //    draw behind them (WindowCompat.setDecorFitsSystemWindows(false) under the hood), and
        //    keeps system-bar icon contrast in sync with light/dark mode. No deprecated window flags.
        //    Per-screen icon color is refined in each Fragment (see WindowInsetsExt).
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)
    }
}
