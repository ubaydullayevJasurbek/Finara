package com.ubaydullayev.expensetracker.domain.model

class UserProfile(
    val name: String,
    val email: String
) {

    val initials: String
        get() = name.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }
}