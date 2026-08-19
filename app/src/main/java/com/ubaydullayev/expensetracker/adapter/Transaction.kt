package com.ubaydullayev.expensetracker.adapter

data class Transaction(
    val id: String,
    val title: String,
    val category: String,
    val categoryColor: Int,
    val categoryBgColor: Int,
    val dateTime: String,
    val amount: Double,
    val isIncome: Boolean,
    val iconRes: Int,
    val iconBgColor: Int
)