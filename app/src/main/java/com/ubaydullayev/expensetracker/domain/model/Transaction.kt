package com.ubaydullayev.expensetracker.domain.model

data class Transaction(
    val id: Long,
    val title: String,
    val category: String,
    val amount: Double,
    val isIncome: Boolean,
    val timestamp: Long,
    val isRecurring: Boolean = false,
    val hasAttachment: Boolean = false,
    val note: String = ""
)