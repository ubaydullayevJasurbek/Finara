package com.ubaydullayev.expensetracker.utils

/**
 * Single source of truth for category names used across the app (seed data, entities, and
 * [CategoryStyleProvider]). Reference these constants instead of typing raw strings so a category
 * always has a matching style — a typo like "Food" vs "Groceries" would otherwise silently fall back
 * to the grey default style.
 */
object TransactionCategory {
    const val INCOME = "Income"
    const val GROCERIES = "Groceries"
    const val TRANSPORT = "Transport"
    const val SHOPPING = "Shopping"
    const val ENTERTAINMENT = "Entertainment"
    const val MUSIC = "Music"
    const val TRAVEL = "Travel"
    const val HOUSING = "Housing"
    const val INSURANCE = "Insurance"
    const val SOFTWARE = "Software"

    /** Every known category, e.g. to populate a category picker. */
    val ALL = listOf(
        INCOME, GROCERIES, TRANSPORT, SHOPPING, ENTERTAINMENT,
        MUSIC, TRAVEL, HOUSING, INSURANCE, SOFTWARE,
    )
}
