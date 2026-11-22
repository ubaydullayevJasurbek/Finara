package com.ubaydullayev.expensetracker.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class BudgetCalculatorTest {

    private fun millis(year: Int, month: Int, day: Int, hour: Int = 12): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day, hour, 0, 0)
        }.timeInMillis

    // ---------- daysLeftInMonth ----------

    @Test
    fun daysLeft_countsToday() {
        // 21-noyabr: 21..30 => 10 kun
        assertEquals(10, BudgetCalculator.daysLeftInMonth(millis(2026, Calendar.NOVEMBER, 21)))
    }

    @Test
    fun daysLeft_lastDayOfMonth_isOne() {
        assertEquals(1, BudgetCalculator.daysLeftInMonth(millis(2026, Calendar.NOVEMBER, 30)))
    }

    @Test
    fun daysLeft_firstDay_isWholeMonth() {
        assertEquals(31, BudgetCalculator.daysLeftInMonth(millis(2026, Calendar.DECEMBER, 1)))
    }

    @Test
    fun daysLeft_leapFebruary() {
        assertEquals(29, BudgetCalculator.daysLeftInMonth(millis(2028, Calendar.FEBRUARY, 1)))
    }

    // ---------- dailyAllowance ----------

    @Test
    fun dailyAllowance_normalCase() {
        // ($1,950 - $1,620) / 10 = $33
        assertEquals(33.0, BudgetCalculator.dailyAllowance(1950.0, 1620.0, 10), 0.0001)
    }

    @Test
    fun dailyAllowance_overBudget_isZero() {
        assertEquals(0.0, BudgetCalculator.dailyAllowance(1000.0, 1200.0, 5), 0.0)
    }

    @Test
    fun dailyAllowance_exactlyAtLimit_isZero() {
        assertEquals(0.0, BudgetCalculator.dailyAllowance(500.0, 500.0, 5), 0.0)
    }

    @Test
    fun dailyAllowance_zeroDaysLeft_treatedAsOne() {
        assertEquals(100.0, BudgetCalculator.dailyAllowance(300.0, 200.0, 0), 0.0001)
    }

    // ---------- monthRange ----------

    @Test
    fun monthRange_startsAtMidnightOfFirstDay_endsAtNextMonth() {
        val (start, end) = BudgetCalculator.monthRange(millis(2026, Calendar.NOVEMBER, 21, 15))
        assertEquals(millis(2026, Calendar.NOVEMBER, 1, 0), start)
        assertEquals(millis(2026, Calendar.DECEMBER, 1, 0), end)
    }

    @Test
    fun monthRange_december_rollsOverToJanuary() {
        val (start, end) = BudgetCalculator.monthRange(millis(2026, Calendar.DECEMBER, 31, 23))
        assertEquals(millis(2026, Calendar.DECEMBER, 1, 0), start)
        assertEquals(millis(2027, Calendar.JANUARY, 1, 0), end)
    }
}