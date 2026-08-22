package com.ubaydullayev.expensetracker.utils

import android.graphics.Color
import com.ubaydullayev.expensetracker.R
import androidx.core.graphics.toColorInt


data class CategoryStyle(
    val iconRes: Int,
    val iconBgColor: Int,
    val textColor: Int

)

object CategoryStyleProvider {

    private val style = mapOf(
        "Income" to CategoryStyle(
            iconRes = R.drawable.ic_dollar,
            iconBgColor = "#E8F5E9".toColorInt(),
            textColor = "#2E7D32".toColorInt()
        ),
        "Groceries" to CategoryStyle(
            iconRes = R.drawable.ic_cart,
            iconBgColor = "#FFF8E1".toColorInt(),
            textColor = "#F9A825".toColorInt()
        ),
        "Transport" to CategoryStyle(
            iconRes = R.drawable.ic_transport,
            iconBgColor = "#EDE7F6".toColorInt(),
            textColor = "#5E35B1".toColorInt()
        ),
        "Shopping" to CategoryStyle(
            iconRes = R.drawable.ic_shopping,
            iconBgColor = "#FFEBEE".toColorInt(),
            textColor = "#E53935".toColorInt()
        ),
        "Entertainment" to CategoryStyle(
            iconRes = R.drawable.ic_play,
            iconBgColor = "#FCE4EC".toColorInt(),
            textColor = "#E91E63".toColorInt()
        ),
        "Music" to CategoryStyle(
            iconRes = R.drawable.ic_music,
            iconBgColor = "#E8F5E9".toColorInt(),
            textColor = "#2E7D32".toColorInt()
        ),
        "Travel" to CategoryStyle(
            iconRes = R.drawable.ic_flight,
            iconBgColor = "#E3F2FD".toColorInt(),
            textColor = "#1976D2".toColorInt()
        )
    )

    private val default = CategoryStyle(
        iconRes = R.drawable.ic_default,
        iconBgColor = "#F5F5F5".toColorInt(),
        textColor = "#9E9E9E".toColorInt()
    )

    fun getStyle(category: String): CategoryStyle {
        return style[category] ?: default
    }
}