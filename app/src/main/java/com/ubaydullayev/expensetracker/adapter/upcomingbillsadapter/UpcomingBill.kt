package com.ubaydullayev.expensetracker.adapter.upcomingbillsadapter

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

/** One upcoming bill row shown in the Home "Upcoming Bills" list. */
data class UpcomingBill(
    val id: String,
    val title: String,
    val category: String,
    val frequency: String,
    val price: Double,
    val dueText: String,
    val isUrgent: Boolean,
    val iconRes: Int,
    val iconBgColor: Int
)