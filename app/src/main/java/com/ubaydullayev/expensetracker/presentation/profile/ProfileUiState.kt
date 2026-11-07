package com.ubaydullayev.expensetracker.presentation.profile

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val initials: String = "?",
    val notificationsEnabled: Boolean = true,
    val currencyCode: String = "USD"
)
