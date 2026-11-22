package com.ubaydullayev.expensetracker.utils

import java.util.Calendar

/**
 * Budjet hisob-kitoblari uchun yagona manba (Home va Budget ekranlari shu yerdan foydalanadi).
 * Android'ga bog'liq emas: oddiy JVM unit test bilan tekshiriladi.
 */
object BudgetCalculator {

    /** Joriy oy chegaralari: [oy boshi, keyingi oy boshi) millisekundlarda. */
    fun monthRange(nowMillis: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = (start.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
        return start.timeInMillis to end.timeInMillis
    }

    /** Oy oxirigacha qolgan kunlar, bugun ham hisobga olinadi (kamida 1). */
    fun daysLeftInMonth(nowMillis: Long = System.currentTimeMillis()): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = cal.get(Calendar.DAY_OF_MONTH)
        return (daysInMonth - today + 1).coerceAtLeast(1)
    }

    /**
     * Kuniga qancha sarflash mumkin: (limit - sarflangan) / qolgan kunlar.
     * Limit oshib ketgan bo'lsa 0 (manfiy "kunlik limit" ma'nosiz).
     */
    fun dailyAllowance(limit: Double, spent: Double, daysLeft: Int): Double {
        val remaining = limit - spent
        if (remaining <= 0.0) return 0.0
        return remaining / daysLeft.coerceAtLeast(1)
    }
}