package com.ubaydullayev.expensetracker.utils

import com.ubaydullayev.expensetracker.domain.model.AppCurrency

/** Barcha kurslar "1 birlik = necha so'm" ko'rinishida (UZS markaz, pivot). */
object ExchangeRates {

    // Faqat birinchi ochilishda, internet va saqlangan kurs bo'lmaganda ishlatiladi.
    // Taxminiy qiymatlar, haqiqiy kurs yuklangach almashadi.
    private val fallback = mapOf(
        "UZS" to 1.0, "USD" to 12000.0, "EUR" to 13500.0, "GBP" to 15800.0,
        "RUB" to 150.0, "KZT" to 23.0, "TRY" to 290.0
    )

    @Volatile
    private var rates: Map<String, Double> = fallback

    fun update(newRates: Map<String, Double>) {
        rates = fallback + newRates // yetishmaydigan valyutani fallback to'ldiradi
    }

    fun convert(amount: Double, from: AppCurrency, to: AppCurrency): Double {
        if (from.code == to.code) return amount
        val fromRate = rates[from.code] ?: return amount
        val toRate = rates[to.code] ?: return amount
        return amount * fromRate / toRate
    }
}