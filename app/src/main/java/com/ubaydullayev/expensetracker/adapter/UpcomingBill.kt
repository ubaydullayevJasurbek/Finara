package com.ubaydullayev.expensetracker.adapter

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

/** One upcoming bill row shown in the Home "Upcoming Bills" list. */
data class UpcomingBill(
    val title: String,
    val category: String,
    val frequency: String,
    val price: String,
    val dueText: String,
    @param:ColorInt val dueColor: Int,
    @param:DrawableRes val iconRes: Int,
)
