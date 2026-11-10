package com.ubaydullayev.expensetracker.domain.model

/**
 * Currencies the user can pick on the Profile screen. Stored in prefs by [code] (ISO 4217).
 */
data class AppCurrency(
    val code: String,
    val name: String,
    val symbol: String
) {
    companion object {
        val USD = AppCurrency("USD", "US Dollar", "$")
        val EUR = AppCurrency("EUR", "Euro", "€")
        val GBP = AppCurrency("GBP", "British Pound", "£")
        val RUB = AppCurrency("RUB", "Russian Ruble", "₽")
        val UZS = AppCurrency("UZS", "Uzbek Som", "so'm")
        val KZT = AppCurrency("KZT", "Kazakhstani Tenge", "₸")
        val TRY = AppCurrency("TRY", "Turkish Lira", "₺")

        val ALL: List<AppCurrency> = listOf(USD, EUR, GBP, RUB, UZS, KZT, TRY)

        /** Unknown or legacy codes fall back to [USD], matching the prefs default. */
        fun fromCode(code: String): AppCurrency =
            ALL.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: USD
    }
}
