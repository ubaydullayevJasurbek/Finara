package com.ubaydullayev.expensetracker.presentation.common

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment

/**
 * Adds the system-bar (status + navigation) and display-cutout insets to this view's padding,
 * on top of whatever padding it already has. The view's *background* still fills behind the bars,
 * so there are no white/black gaps — only the content is inset. Heights are read from live
 * WindowInsets (never hardcoded), so it stays correct across devices, rotation and gesture/3-button nav.
 *
 * Pass `applyTop = false` for content that should extend behind the status bar (hero images), or
 * `applyBottom = false` for content that should extend behind the navigation bar.
 */
fun View.applySystemBarInsetsAsPadding(
    applyLeft: Boolean = true,
    applyTop: Boolean = true,
    applyRight: Boolean = true,
    applyBottom: Boolean = true,
) {
    // Capture the design-time padding once so repeated inset passes don't accumulate.
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        v.updatePadding(
            left = initialLeft + if (applyLeft) bars.left else 0,
            top = initialTop + if (applyTop) bars.top else 0,
            right = initialRight + if (applyRight) bars.right else 0,
            bottom = initialBottom + if (applyBottom) bars.bottom else 0,
        )
        insets // pass through so sibling/child listeners still receive the insets
    }
    // Make sure a pass runs even if the view is already attached when this is called.
    if (isAttachedToWindow) ViewCompat.requestApplyInsets(this) else addOnAttachStateChangeListener(
        object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                ViewCompat.requestApplyInsets(v)
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        }
    )
}

/**
 * Chooses the status-bar icon color for the current screen's background.
 * @param light true = dark icons (for light backgrounds), false = white icons (for dark backgrounds).
 */
fun Fragment.setLightStatusBars(light: Boolean) {
    val window = requireActivity().window
    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = light
}
