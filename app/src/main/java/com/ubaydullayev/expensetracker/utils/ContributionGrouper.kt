package com.ubaydullayev.expensetracker.utils

import com.ubaydullayev.expensetracker.domain.model.Contribution
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class MonthlyContribution(
    val monthLabel: String,   // "Oct", "Nov" va h.k.
    val totalAmount: Double
)

object ContributionGrouper {

    fun groupByLastSixMonths(contributions: List<Contribution>): List<MonthlyContribution> {
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Oxirgi 6 oyning "kalit"larini (masalan "2026-10") tayyorlaymiz, eng eskisidan boshlab
        val monthKeys = mutableListOf<String>()
        val monthLabels = mutableListOf<String>()

        for (i in 5 downTo 0) {
            val cal = calendar.clone() as Calendar
            cal.add(Calendar.MONTH, -i)
            val key = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
            monthKeys.add(key)
            monthLabels.add(monthFormat.format(cal.time))
        }

        // Har bir kontribyutsiyani tegishli oy kalitiga guruhlaymiz
        val grouped = contributions.groupBy {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
        }

        // Har bir oy uchun (hatto ma'lumot bo'lmasa ham, 0 bilan) natija tuzamiz
        return monthKeys.mapIndexed { index, key ->
            val total = grouped[key]?.sumOf { it.amount } ?: 0.0
            MonthlyContribution(monthLabels[index], total)
        }
    }

    fun calculateAverage(monthly: List<MonthlyContribution>): Double {
        if (monthly.isEmpty()) return 0.0
        return monthly.sumOf { it.totalAmount } / monthly.size
    }
}