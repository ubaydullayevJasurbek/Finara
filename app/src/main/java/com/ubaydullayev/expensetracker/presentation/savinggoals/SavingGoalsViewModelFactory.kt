package com.ubaydullayev.expensetracker.presentation.savinggoals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ubaydullayev.expensetracker.domain.repository.ContributionRepository
import com.ubaydullayev.expensetracker.domain.repository.SavingGoalRepository

class SavingGoalsViewModelFactory(
    private val savingGoalRepository: SavingGoalRepository,
    private val contributionRepository: ContributionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SavingGoalsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SavingGoalsViewModel(savingGoalRepository, contributionRepository) as T
        }
        throw IllegalArgumentException("Noma'lum ViewModel klassi")
    }
}