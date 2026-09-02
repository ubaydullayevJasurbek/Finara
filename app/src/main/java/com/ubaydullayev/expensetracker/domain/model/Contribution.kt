package com.ubaydullayev.expensetracker.domain.model

data class Contribution(
    val id: Long,
    val goalId: Long,
    val amount: Double,
    val timestamp: Long
)
