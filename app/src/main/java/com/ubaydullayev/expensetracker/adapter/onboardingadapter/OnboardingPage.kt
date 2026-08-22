package com.ubaydullayev.expensetracker.adapter.onboardingadapter

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

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