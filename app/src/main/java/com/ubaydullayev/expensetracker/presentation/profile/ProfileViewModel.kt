package com.ubaydullayev.expensetracker.presentation.profile

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ubaydullayev.expensetracker.data.local.prefs.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel(private val prefs: UserPreferences) : ViewModel() {


    val uiState: StateFlow<ProfileUiState> = combine(
        prefs.profile,
        prefs.notificationsEnabled,
        prefs.currencyCode
    ) { profile, notifications, currency ->

        ProfileUiState(
            name = profile.name,
            email = profile.email,
            initials = profile.initials,
            notificationsEnabled = notifications,
            currencyCode = currency
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())


    fun updateProfile(name: String, email: String): Boolean {
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        if (cleanName.isEmpty()) return false
        if (cleanEmail.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return false
        }
        prefs.updateProfile(cleanName, cleanEmail)
        return true
    }
    fun setNotificationsEnabled(enabled: Boolean) = prefs.setNotificationsEnabled(enabled)

    fun setCurrency(code: String) = prefs.setCurrencyCode(code)
}