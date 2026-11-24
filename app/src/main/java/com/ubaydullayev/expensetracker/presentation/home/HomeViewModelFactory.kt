package com.ubaydullayev.expensetracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ubaydullayev.expensetracker.domain.repository.BudgetRepository
import com.ubaydullayev.expensetracker.domain.repository.SavingGoalRepository
import com.ubaydullayev.expensetracker.domain.repository.TransactionRepository

class HomeViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val savingGoalRepository: SavingGoalRepository,
    private val budgetRepository: BudgetRepository,
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
       if (modelClass.isAssignableFrom(HomeViewModel::class.java)){
           @Suppress("UNCHECKED_CAST")
           return HomeViewModel(
               transactionRepository,
               savingGoalRepository,
               budgetRepository

           )as T
       }
        throw IllegalArgumentException("Noma'lum ViewModel klassi")
    }
}