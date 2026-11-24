package com.ubaydullayev.expensetracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ubaydullayev.expensetracker.domain.model.Budget
import com.ubaydullayev.expensetracker.domain.model.SavingGoal
import com.ubaydullayev.expensetracker.domain.model.Transaction
import com.ubaydullayev.expensetracker.domain.model.RecurringPayment
import com.ubaydullayev.expensetracker.domain.repository.BudgetRepository
import com.ubaydullayev.expensetracker.domain.repository.SavingGoalRepository
import com.ubaydullayev.expensetracker.domain.repository.TransactionRepository
import com.ubaydullayev.expensetracker.utils.DateFormatter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.ubaydullayev.expensetracker.utils.BudgetCalculator
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Home ekranining holati: balans, tejash foizi, Safe to spend, Insight va ro'yxatlar.
 */
class HomeViewModel(
    private val transactionRepository: TransactionRepository,
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

    /**
     * "Recurring" belgili xarajatlardan hisoblangan keyingi to'lovlar (eng yaqini birinchi).
     * Alohida jadval yo'q: foydalanuvchi qo'shimcha hech narsa kiritmaydi.
     */
    val recurringPayments: StateFlow<List<RecurringPayment>> =
        transactionRepository.getAllTransactions()
            .map { RecurringPayment.fromTransactions(it, System.currentTimeMillis()) }
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
                ((currentMonthBalance - previousMonthBalance) / abs(previousMonthBalance)) * 100
            } else {
                null
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

    /** null -> karta yashiriladi (na budjet, na daromad bor). */
    val safeToSpend: StateFlow<SafeToSpend?> =
        combine(budgets, balanceSummary) { budgetList, summary ->
            calculateSafeToSpend(budgetList, summary)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** null -> karta yashiriladi (shu oy xarajat yo'q). */
    val insight: StateFlow<SpendingInsight?> =
        transactionRepository.getAllTransactions()
            .map { calculateInsight(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun calculateSafeToSpend(
        budgetList: List<Budget>,
        summary: BalanceSummary
    ): SafeToSpend? {
        val totalBudget = budgetList.sumOf { it.limitAmount }
        val basedOnBudget = totalBudget > 0.0
        val limit = if (basedOnBudget) totalBudget else summary.monthlyIncome
        if (limit <= 0.0) return null

        // Budjet bo'lsa: Budget ekranidagi bilan aynan bir xil raqam (budjetli kategoriyalar xarajati).
        // Budjet bo'lmasa: shu oygi daromad va barcha xarajatlar asosida.
        val spent =
            if (basedOnBudget) budgetList.sumOf { it.spentAmount } else summary.monthlyExpense
        val daysLeft = BudgetCalculator.daysLeftInMonth()
        val perDay = BudgetCalculator.dailyAllowance(limit, spent, daysLeft)

        return SafeToSpend(perDay, spent, limit, daysLeft, basedOnBudget)
    }

    private fun calculateInsight(transactions: List<Transaction>): SpendingInsight? {
        val monthStart = DateFormatter.getCurrentMonthStart()
        val (prevStart, prevEnd) = DateFormatter.getPreviousMonthRange()
        val expenses = transactions.filter { !it.isIncome }

        val current = expenses.filter { it.timestamp >= monthStart }
            .groupBy { it.category }.mapValues { (_, list) -> list.sumOf { it.amount } }
        if (current.isEmpty()) return null

        val previous = expenses.filter { it.timestamp in prevStart..prevEnd }
            .groupBy { it.category }.mapValues { (_, list) -> list.sumOf { it.amount } }

        // 1) O'tgan oyga nisbatan eng ko'p o'zgargan kategoriya (kamida 10%)
        val change = current.mapNotNull { (category, amount) ->
            val before = previous[category] ?: return@mapNotNull null
            if (before <= 0.0) return@mapNotNull null
            category to ((amount - before) / before) * 100
        }.filter { abs(it.second) >= 10 }.maxByOrNull { abs(it.second) }

        if (change != null) {
            val (category, percent) = change
            val p = abs(percent).roundToInt()
            val text = if (percent > 0) "You spent $p% more on $category than last month."
            else "You spent $p% less on $category than last month. Nice!"
            return SpendingInsight(text, "vs ${previousMonthName()}")
        }

        // 2) O'tgan oy bilan solishtirib bo'lmasa: eng katta xarajat kategoriyasi ulushi
        val total = current.values.sum()
        val (topCategory, topAmount) = current.maxByOrNull { it.value } ?: return null
        val share = ((topAmount / total) * 100).roundToInt()
        return SpendingInsight("$topCategory is $share% of your spending this month.", "This month")
    }

    private fun previousMonthName(): String {
        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        return cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH) ?: ""
    }

    private fun calculateFinancialHealth(income: Double, expense: Double): FinancialHealth {
        if (income <= 0.0) {
            return FinancialHealth(
                score = 0,
                label = "No Data",
                message = "Add income to see your financial health."
            )
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