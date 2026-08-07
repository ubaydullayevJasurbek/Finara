package com.ubaydullayev.expensetracker.adapter

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

/** One onboarding page (Figma 253:14..253:17). All per-page values live here. */
data class OnboardingPage(
    @param:DrawableRes val panelBg: Int,
    @param:DrawableRes val glyph: Int,
    val chip: String,
    @param:ColorRes val chipBg: Int,
    @param:ColorRes val chipText: Int,
    val pill1: String,
    @param:ColorRes val pill1Dot: Int,
    val pill2: String,
    @param:ColorRes val pill2Dot: Int,
    val title: CharSequence,
    val subtitle: String,
)
