package com.ubaydullayev.expensetracker.presentation.transaction

/**
 * Ro'yxat turi bo'yicha filtr. Sana bo'yicha tanlov endi tepadagi oy
 * almashtirgichda (◀ November 2026 ▶), shuning uchun bu yerda faqat tur.
 */
enum class TransactionFilter {
    ALL,
    INCOME,
    EXPENSE,
    RECURRING
}