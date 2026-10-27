package com.ubaydullayev.expensetracker.presentation.budget


data class BudgetCategory(
    val name: String,
    val forecastText: String,
    val spent: Int,
    val limit: Int,
    val percent: Int,
    val leftText: String,
    val status: BudgetStatus,
    val isOverForecast: Boolean,
    val iconRes: Int,
    val iconBgColor: Int,
    val iconTint: Int
)