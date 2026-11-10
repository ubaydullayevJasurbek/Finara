package com.ubaydullayev.expensetracker.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ubaydullayev.expensetracker.data.local.prefs.UserPreferences
import com.ubaydullayev.expensetracker.domain.repository.TransactionRepository

class ProfileViewModelFactory(
    private val prefs: UserPreferences,
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(prefs, transactionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
