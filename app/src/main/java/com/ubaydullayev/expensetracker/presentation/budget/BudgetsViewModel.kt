package com.ubaydullayev.expensetracker.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ubaydullayev.expensetracker.domain.model.Budget
import com.ubaydullayev.expensetracker.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BudgetsViewModel(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    val uiState: StateFlow<BudgetUiState> =
        budgetRepository.getAllBudgets()
            .map { budgets -> buildUiState(budgets) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetUiState())

    private fun buildUiState(budgets: List<Budget>): BudgetUiState {
        val totalSpent = budgets.sumOf { it.spentAmount }
        val totalLimit = budgets.sumOf { it.limitAmount }
        val remaining = totalLimit - totalSpent

        val spentPercent = if (totalLimit > 0) {
            ((totalSpent / totalLimit) * 100).toInt()
        } else {
            0
        }

        val dailyLeft = calculateDailyLeft(remaining)
        val statusText = buildStatusText(spentPercent)
        val smartTip = buildSmartTip(budgets)
        val reduceTarget = findReduceTarget(budgets)
        val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            .format(System.currentTimeMillis())

        return BudgetUiState(
            monthLabel = monthLabel,
            totalSpent = totalSpent,
            totalLimit = totalLimit,
            remaining = remaining,
            dailyLeft = dailyLeft,
            spentPercent = spentPercent,
            statusText = statusText,
            smartTipText = smartTip,
            smartTipCategory = reduceTarget?.first,
            smartTipReduceBy = reduceTarget?.second ?: 0.0,
            categories = budgets
        )
    }

    private fun calculateDailyLeft(remaining: Double): Double {
        val calendar = Calendar.getInstance()
        val totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val daysLeft = (totalDaysInMonth - currentDay + 1).coerceAtLeast(1)

        return remaining / daysLeft
    }

    fun addBudget(category: String, limitAmount: Double) {
        viewModelScope.launch {
            budgetRepository.addBudget(
                Budget(
                    id = 0,
                    title = category,
                    category = category,
                    spentAmount = 0.0,
                    limitAmount = limitAmount
                )
            )
        }
    }

    private fun buildStatusText(percent: Int): String {
        return when {
            percent < 80 -> "On track · $percent% used"
            percent < 100 -> "Near limit · $percent% used"
            else -> "Over budget · $percent% used"
        }
    }

    /** Pulsiz tiplar uchun matn. Summali "Reduce ..." tipi [findReduceTarget] orqali UI da quriladi. */
    private fun buildSmartTip(budgets: List<Budget>): String {
        if (budgets.isEmpty()) return "Add a budget to get personalized tips."
        if (findReduceTarget(budgets) != null) return ""
        return "You're doing great! All categories are within budget."
    }

    /** Eng xavfli kategoriya va 80% chegaradan oshgan summa (home valyutada), yoki null */
    private fun findReduceTarget(budgets: List<Budget>): Pair<String, Double>? {
        val riskiest = budgets
            .filter { it.limitAmount > 0 }
            .maxByOrNull { it.spentAmount / it.limitAmount } ?: return null
        val overAmount = riskiest.spentAmount - (riskiest.limitAmount * 0.8)
        return if (overAmount > 0) riskiest.title to overAmount else null
    }
    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budget)
        }
    }
}