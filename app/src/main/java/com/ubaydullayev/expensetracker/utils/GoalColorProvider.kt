package com.ubaydullayev.expensetracker.utils

import com.ubaydullayev.expensetracker.R
import androidx.core.graphics.toColorInt

data class GoalColorScheme(
    val bannerBg: Int,
    val progressColor: Int,
    val tagBgColor: Int,
    val tagTextColor: Int,
    val iconRes: Int
)

object GoalColorProvider {

    private val schemes = mapOf(
        "Travel" to GoalColorScheme(
            bannerBg = R.drawable.bg_goal_banner_blue,
            progressColor = "#3359D9".toColorInt(),
            tagBgColor = "#DBEAFE".toColorInt(),
            tagTextColor = "#2E65C0".toColorInt(),
            iconRes = R.drawable.ic_plane
        ),
        "Safety" to GoalColorScheme(
            bannerBg = R.drawable.bg_goal_banner_green,
            progressColor = "#0B7E57".toColorInt(),
            tagBgColor = "#DEF7EC".toColorInt(),
            tagTextColor = "#0B7E57".toColorInt(),
            iconRes = R.drawable.ic_shield
        ),
        "Tech" to GoalColorScheme(
            bannerBg = R.drawable.bg_goal_banner_purple,
            progressColor = "#7C3AED".toColorInt(),
            tagBgColor = "#EDE9FE".toColorInt(),
            tagTextColor = "#7C3AED".toColorInt(),
            iconRes = R.drawable.ic_laptop
        )
    )

    private val default = GoalColorScheme(
        bannerBg = R.drawable.bg_goal_banner_blue,
        progressColor = "#3359D9".toColorInt(),
        tagBgColor = "#DBEAFE".toColorInt(),
        tagTextColor = "#2E65C0".toColorInt(),
        iconRes = R.drawable.ic_grid
    )

    fun getScheme(category: String): GoalColorScheme {
        return schemes[category] ?: default
    }
}