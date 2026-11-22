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
    // Summalar raqam holida saqlanadi, UI valyutada formatlaydi (formatAmount)
    val smartTip: SmartTip = SmartTip.NoBudgets,
    val categories: List<Budget> = emptyList(),
    // Limitdan oshgan kategoriyalar soni: >0 bo'lsa umumiy holat ham ogohlantiradi
    val overBudgetCount: Int = 0
)

/** Smart tip turi — eng xavfli budjet (spent/limit eng katta) asosida. */
sealed interface SmartTip {
    data object NoBudgets : SmartTip
    data object AllGood : SmartTip
    data class OverBudget(val category: String, val overAmount: Double) : SmartTip
    data class NearLimit(val category: String, val left: Double, val perDay: Double) : SmartTip
}