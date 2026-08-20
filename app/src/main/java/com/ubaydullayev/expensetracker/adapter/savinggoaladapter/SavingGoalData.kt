package com.ubaydullayev.expensetracker.adapter.savinggoaladapter

data class SavingGoalData(
    val id: String,
    val title: String,
    val category: String,
    val estimatedDate: String,
    val priority: String,
    val targetDate: String,
    val currentAmount: Double,
    val targetAmount: Double,
    val iconRes: Int
)