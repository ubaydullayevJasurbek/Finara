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

/**
 * "Safe to spend today": oy oxirigacha har kuni qancha sarflash mumkin.
 * limit = budjetlar yig'indisi; budjet bo'lmasa shu oygi daromad.
 */
data class SafeToSpend(
    val perDay: Double,
    val spent: Double,
    val limit: Double,
    val daysLeft: Int,
    val basedOnBudget: Boolean
) {
    val isOverLimit: Boolean get() = spent >= limit
    val progressPercent: Int get() = ((spent / limit) * 100).toInt().coerceIn(0, 100)
}

/** Shu oy xarajatlaridan hisoblangan bitta xulosa (soxta matn emas). */
data class SpendingInsight(
    val text: String,
    val period: String
)