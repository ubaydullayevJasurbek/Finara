package com.ubaydullayev.expensetracker.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ubaydullayev.expensetracker.domain.model.Budget
import com.ubaydullayev.expensetracker.domain.repository.BudgetRepository
import com.ubaydullayev.expensetracker.utils.BudgetCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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

        val dailyLeft = BudgetCalculator.dailyAllowance(
            totalLimit,
            totalSpent,
            BudgetCalculator.daysLeftInMonth()
        )
        val overBudgetCount =
            budgets.count { it.limitAmount > 0 && it.spentAmount > it.limitAmount }
        val statusText = buildStatusText(spentPercent, overBudgetCount)
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
            smartTip = smartTip,
            categories = budgets,
            overBudgetCount = overBudgetCount
        )
    }

    /** Bir kategoriyaga faqat bitta budjet (aks holda bitta xarajat ikki marta hisoblanadi). */
    fun hasBudgetFor(category: String): Boolean =
        uiState.value.categories.any { it.category.equals(category, ignoreCase = true) }

    fun addBudget(category: String, limitAmount: Double) {
        if (hasBudgetFor(category)) return
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

    private fun buildStatusText(percent: Int, overBudgetCount: Int): String {
        // Biror kategoriya limitdan oshgan bo'lsa, umumiy foiz past bo'lsa ham ogohlantiramiz
        if (overBudgetCount > 0) {
            val noun = if (overBudgetCount == 1) "category" else "categories"
            return "$overBudgetCount $noun over budget · $percent% used"
        }
        return when {
            percent < 80 -> "On track · $percent% used"
            percent < 100 -> "Near limit · $percent% used"
            else -> "Over budget · $percent% used"
        }
    }

    /** Eng xavfli budjet (spent/limit eng katta) asosida tip. Summalar home valyutada. */
    private fun buildSmartTip(budgets: List<Budget>): SmartTip {
        if (budgets.isEmpty()) return SmartTip.NoBudgets
        val riskiest = budgets
            .filter { it.limitAmount > 0 }
            .maxByOrNull { it.spentAmount / it.limitAmount } ?: return SmartTip.AllGood
        val spent = riskiest.spentAmount
        val limit = riskiest.limitAmount
        return when {
            spent > limit -> SmartTip.OverBudget(riskiest.title, spent - limit)
            spent >= limit * 0.8 -> SmartTip.NearLimit(
                category = riskiest.title,
                left = limit - spent,
                perDay = BudgetCalculator.dailyAllowance(
                    limit, spent, BudgetCalculator.daysLeftInMonth()
                )
            )
            else -> SmartTip.AllGood
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budget)
        }
    }
}