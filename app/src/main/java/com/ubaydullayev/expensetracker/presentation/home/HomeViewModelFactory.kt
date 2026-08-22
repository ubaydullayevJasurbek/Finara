package com.ubaydullayev.expensetracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ubaydullayev.expensetracker.domain.repository.SavingGoalRepository
import com.ubaydullayev.expensetracker.domain.repository.TransactionRepository
import com.ubaydullayev.expensetracker.domain.repository.UpcomingBillRepository

class HomeViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val upcomingBillRepository: UpcomingBillRepository,
    private val savingGoalRepository: SavingGoalRepository
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
       if (modelClass.isAssignableFrom(HomeViewModel::class.java)){
           @Suppress("UNCHECKED_CAST")
           return HomeViewModel(
               transactionRepository,
               upcomingBillRepository,
               savingGoalRepository
           )as T
       }
        throw IllegalArgumentException("Noma'lum ViewModel klassi")
    }
}