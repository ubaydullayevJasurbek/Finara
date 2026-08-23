package com.ubaydullayev.expensetracker.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateFormatter {

    fun formatTransactionDate(timestamp: Long): String {
        val now = Calendar.getInstance()
        val txDate = Calendar.getInstance().apply { timeInMillis = timestamp }

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        return when {
            isSameDay(now, txDate) -> "Today, ${timeFormat.format(txDate.time)}"
            isYesterday(now, txDate) -> "Yesterday, ${timeFormat.format(txDate.time)}"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(txDate.time)
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(now: Calendar, txDate: Calendar): Boolean {
        val yesterday = now.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(yesterday, txDate)
    }

    /** Joriy oyning 1-kuni, 00:00:00 vaqtidagi timestamp — SQL filtrlash uchun */
    fun getCurrentMonthStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /** O'tgan oyning boshlanish va tugash timestamp'lari — Trend hisoblash uchun */
    fun getPreviousMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val monthEnd = cal.timeInMillis   // shu oyning boshi = o'tgan oy tugagan payt

        cal.add(Calendar.MONTH, -1)
        val monthStart = cal.timeInMillis

        return Pair(monthStart, monthEnd)
    }
}