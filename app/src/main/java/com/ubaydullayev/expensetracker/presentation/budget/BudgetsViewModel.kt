package com.ubaydullayev.expensetracker.presentation.budget

import android.annotation.SuppressLint
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

    @SuppressLint("DefaultLocale")
    private fun buildSmartTip(budgets: List<Budget>): String {
        if (budgets.isEmpty()) return "Add a budget to get personalized tips."

        val riskiest = budgets.maxByOrNull { it.spentAmount / it.limitAmount }
            ?: return ""

        val overAmount = riskiest.spentAmount - (riskiest.limitAmount * 0.8)

        return if (overAmount > 0) {
            "Reduce ${riskiest.title} by $${
                String.format(
                    "%,.0f",
                    overAmount
                )
            } to stay under budget this month."
        } else {
            "You're doing great! All categories are within budget."
        }
    }
    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budget)
        }
    }
}