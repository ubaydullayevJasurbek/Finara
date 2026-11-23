package com.ubaydullayev.expensetracker.domain.model

class UserProfile(
    val name: String,
    val email: String
) {

    /** Ism bo'lmasa emailning "@" dan oldingi qismi, u ham bo'lmasa umumiy yozuv. */
    val displayName: String
        get() = name.trim().ifEmpty { email.substringBefore('@').trim() }.ifEmpty { FALLBACK_NAME }

    val initials: String
        get() = name.trim().ifEmpty { email.substringBefore('@').trim() }
            .split(Regex("[\\s._-]+"))
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }

    private companion object {
        const val FALLBACK_NAME = "Finara user"
    }
}
