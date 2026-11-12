package com.ubaydullayev.expensetracker.presentation.home

data class BalanceSummary(
    val totalBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    // null = o'tgan oyda ma'lumot yo'q (0 ga bo'linmaydi) -> badge yashiriladi
    val trendPercent: Double? = null
)

data class FinancialHealth(
    val score: Int = 0,
    val label: String = "",
    val message: String = ""
)