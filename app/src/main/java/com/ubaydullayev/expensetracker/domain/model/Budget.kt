package com.ubaydullayev.expensetracker.domain.model

data class Budget(
    val id: Long,
    val title: String,
    val category: String,
    val spentAmount: Double,
    val limitAmount: Double
)