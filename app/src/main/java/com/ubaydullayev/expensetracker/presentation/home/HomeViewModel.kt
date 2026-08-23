package com.ubaydullayev.expensetracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ubaydullayev.expensetracker.domain.model.Budget
import com.ubaydullayev.expensetracker.domain.model.SavingGoal
import com.ubaydullayev.expensetracker.domain.model.Transaction
import com.ubaydullayev.expensetracker.domain.model.UpcomingBill
import com.ubaydullayev.expensetracker.domain.repository.BudgetRepository
import com.ubaydullayev.expensetracker.domain.repository.SavingGoalRepository
import com.ubaydullayev.expensetracker.domain.repository.TransactionRepository
import com.ubaydullayev.expensetracker.domain.repository.UpcomingBillRepository
import com.ubaydullayev.expensetracker.utils.DateFormatter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map

/**
 * Starting point for the Home screen's UI state/logic. Empty for now — add StateFlow/LiveData and
 * use-cases here as the feature grows.
 */
class HomeViewModel(
    private val transactionRepository: TransactionRepository,
    private val upcomingBillRepository: UpcomingBillRepository,
    private val savingGoalRepository: SavingGoalRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    val budgets: StateFlow<List<Budget>> =
        budgetRepository.getAllBudgets()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<Transaction>> =
        transactionRepository.getRecentTransactions(4)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val upcomingBill: StateFlow<List<UpcomingBill>> =
        upcomingBillRepository.getAllBills()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val savingGoals: StateFlow<List<SavingGoal>> =
        savingGoalRepository.getAllGoals()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val balanceSummary: StateFlow<BalanceSummary> = run {
        val monthStart = DateFormatter.getCurrentMonthStart()
        val (prevStart, prevEnd) = DateFormatter.getPreviousMonthRange()

        combine(
            transactionRepository.getTotalBalance(),
            transactionRepository.getMonthlyIncome(monthStart),
            transactionRepository.getMonthlyExpense(monthStart),
            transactionRepository.getIncomeBetween(prevStart, prevEnd),
            transactionRepository.getExpenseBetween(prevStart, prevEnd)
        ) { values ->
            val totalBalance = values[0]
            val monthlyIncome = values[1]
            val monthlyExpense = values[2]
            val prevIncome = values[3]
            val prevExpense = values[4]

            val currentMonthBalance = monthlyIncome - monthlyExpense
            val previousMonthBalance = prevIncome - prevExpense

            val trend = if (previousMonthBalance != 0.0) {
                ((currentMonthBalance - previousMonthBalance) / kotlin.math.abs(previousMonthBalance)) * 100
            } else {
                0.0
            }
            BalanceSummary(
                totalBalance = totalBalance,
                monthlyIncome = monthlyIncome,
                monthlyExpense = monthlyExpense,
                trendPercent = trend
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BalanceSummary())

    val financialHealth: StateFlow<FinancialHealth> = balanceSummary
        .map { summary ->
            calculateFinancialHealth(summary.monthlyIncome, summary.monthlyExpense)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialHealth())
    private fun calculateFinancialHealth(income: Double, expense: Double): FinancialHealth {
        if (income <= 0.0) {
            return FinancialHealth(score = 0, label = "No Data", message = "Add income to see your financial health.")
        }

        val savingsRate = ((income - expense) / income) * 100
        val score = savingsRate.coerceIn(0.0, 100.0).toInt()

        val label = when {
            score >= 20 -> "Excellent"
            score >= 10 -> "Good"
            score >= 0 -> "Needs Attention"
            else -> "Overspending"
        }

        val message = if (savingsRate >= 0) {
            "Great! You're saving ${score}% of income."
        } else {
            "You're spending more than you earn this month."
        }

        return FinancialHealth(score = score, label = label, message = message)
    }
}
