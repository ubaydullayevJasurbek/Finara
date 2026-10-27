package com.ubaydullayev.expensetracker.presentation.budget


enum class BudgetStatus(val label: String, val main: Int, val soft: Int) {
    SAFE("Safe", 0xFF0F7B55.toInt(), 0xFFDDF5E8.toInt()),
    WARNING("Warning", 0xFFB7791F.toInt(), 0xFFFDF0C4.toInt()),
    OVERSPENT("Overspent", 0xFFD92D20.toInt(), 0xFFFEE4E2.toInt()),
    COMPLETED("Completed", 0xFF4B5563.toInt(), 0xFFEEF0F3.toInt())
}