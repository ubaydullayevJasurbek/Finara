package com.ubaydullayev.expensetracker.domain.model

data class UpcomingBill(
    val id: Long,
    val title: String,
    val category: String,
    val frequency: String,
    val price: Double,
    val dueText: String,
    val isUrgent: Boolean
)