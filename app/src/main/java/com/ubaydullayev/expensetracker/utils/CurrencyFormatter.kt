package com.ubaydullayev.expensetracker.utils

import com.ubaydullayev.expensetracker.domain.model.AppCurrency
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {

    /** Profile'da tanlangan, ekranda ko'rinadigan valyuta */
    @Volatile var current: AppCurrency = AppCurrency.USD

    /** Bazadagi raqamlar saqlangan valyuta */
    @Volatile var home: AppCurrency = AppCurrency.USD

    /** Bazadagi (home) summani ko'rinish valyutasida chiroyli matnga aylantiradi */
    fun format(amountInHome: Double): String {
        val converted = ExchangeRates.convert(amountInHome, home, current)
        val rounded = BigDecimal(converted).setScale(current.decimals, RoundingMode.HALF_UP).toDouble()

        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = current.groupingSeparator
        }
        val pattern = if (current.decimals == 0 || rounded % 1.0 == 0.0) "#,##0" else "#,##0.00"
        // Minus belgisi valyuta belgisidan oldin turadi: "-$100", "-1 250 so'm" ("$-100" emas)
        val minus = if (rounded < 0) "-" else ""
        val number = DecimalFormat(pattern, symbols).format(kotlin.math.abs(rounded))

        return if (current.symbolAfter) "$minus$number ${current.symbol}" else "$minus${current.symbol}$number"
    }

    /** Foydalanuvchi ko'rinish valyutasida kiritgan summani bazaga (home) o'giradi */
    fun toHome(inputInCurrent: Double): Double =
        ExchangeRates.convert(inputInCurrent, current, home)

    /** Bazadagi summani ko'rinish valyutasidagi raqamga o'giradi (matnsiz) */
    fun fromHome(amountInHome: Double): Double =
        ExchangeRates.convert(amountInHome, home, current)
}

/** Ilova bo'ylab yagona pul formatlash nuqtasi — valyuta belgisini o'zi qo'shadi, tashqarida "$" yozilmasin. */
fun formatAmount(amount: Double): String = CurrencyFormatter.format(amount)
