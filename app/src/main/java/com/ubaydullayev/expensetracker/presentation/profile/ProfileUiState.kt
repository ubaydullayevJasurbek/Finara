package com.ubaydullayev.expensetracker.presentation.profile

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val initials: String = "?",
    val currencyCode: String = "USD",
    val hideBalanceOnStart: Boolean = false,
    val transactionCount: Int? = null,
    val goalCount: Int? = null,
    /** Masalan "Nov 2026"; Firebase ma'lumot bermasa null. */
    val memberSince: String? = null,
    /** Sign out / Reset / Delete bajarilmoqda: tugmalar bloklanadi, indikator ko'rinadi. */
    val busy: Boolean = false
)
