package com.ubaydullayev.expensetracker.domain.model

data class SavingGoal(
    val id: Long,
    val title: String,
    val category: String,
    val estimatedDate: String,
    val priority: String,
    val targetDate: String,
    val currentAmount: Double,
    val targetAmount: Double
)