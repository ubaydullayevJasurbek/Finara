package com.ubaydullayev.expensetracker.presentation.budget

import com.ubaydullayev.expensetracker.domain.model.Budget

data class BudgetUiState(
    val monthLabel: String = "",
    val totalSpent: Double = 0.0,
    val totalLimit: Double = 0.0,
    val remaining: Double = 0.0,
    val dailyLeft: Double = 0.0,
    val spentPercent: Int = 0,
    val statusText: String = "",
    val smartTipText: String = "",
    val categories: List<Budget> = emptyList()
)