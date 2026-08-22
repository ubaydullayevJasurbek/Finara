package com.ubaydullayev.expensetracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ubaydullayev.expensetracker.domain.model.SavingGoal
import com.ubaydullayev.expensetracker.domain.model.Transaction
import com.ubaydullayev.expensetracker.domain.model.UpcomingBill
import com.ubaydullayev.expensetracker.domain.repository.SavingGoalRepository
import com.ubaydullayev.expensetracker.domain.repository.TransactionRepository
import com.ubaydullayev.expensetracker.domain.repository.UpcomingBillRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Starting point for the Home screen's UI state/logic. Empty for now — add StateFlow/LiveData and
 * use-cases here as the feature grows.
 */
class HomeViewModel(
    private val transactionRepository: TransactionRepository,
    private val upcomingBillRepository: UpcomingBillRepository,
    private val savingGoalRepository: SavingGoalRepository,
) : ViewModel() {

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
}
