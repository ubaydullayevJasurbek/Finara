package com.ubaydullayev.expensetracker.domain.model

data class AppCurrency(
    val code: String,
    val symbol: String,
    val name: String,
    val symbolAfter: Boolean = false,
    val groupingSeparator: Char = ',',
    val decimals: Int = 2
) {
    companion object {
        val USD = AppCurrency("USD", "$", "US Dollar")
        val ALL = listOf(
            USD,
            AppCurrency("EUR", "€", "Euro"),
            AppCurrency("GBP", "£", "British Pound"),
            AppCurrency("UZS", "so'm", "Uzbek Som", symbolAfter = true, groupingSeparator = ' ', decimals = 0),
            AppCurrency("RUB", "₽", "Russian Ruble", symbolAfter = true, groupingSeparator = ' '),
            AppCurrency("KZT", "₸", "Kazakh Tenge", symbolAfter = true, groupingSeparator = ' '),
            AppCurrency("TRY", "₺", "Turkish Lira")
        )

        fun fromCode(code: String): AppCurrency = ALL.firstOrNull { it.code == code } ?: USD
    }
}