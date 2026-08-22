package com.ubaydullayev.expensetracker.domain.model

data class Transaction(
    val id: Long,
    val title: String,
    val category: String,
    val dateTime: String,
    val amount: Double,
    val isIncome: Boolean
)