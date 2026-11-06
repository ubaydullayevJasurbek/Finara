package com.ubaydullayev.expensetracker.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ubaydullayev.expensetracker.domain.repository.BudgetRepository

@Suppress("UNCHECKED_CAST")
class BudgetsViewModelFactory(private val budgetRepository: BudgetRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetsViewModel::class.java)) {
            return BudgetsViewModel(budgetRepository) as T
        }
        throw IllegalArgumentException("Noma'lum ViewModel klassi")
    }
}