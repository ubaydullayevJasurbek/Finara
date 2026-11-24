package com.ubaydullayev.expensetracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class RecurringPaymentTest {

    private val tz = TimeZone.getTimeZone("Asia/Tashkent")

    /** 2026-11-24 10:00 */
    private val now = date(2026, Calendar.NOVEMBER, 24, hour = 10)

    private fun date(year: Int, month: Int, day: Int, hour: Int = 12): Long =
        Calendar.getInstance(tz).apply {
            clear()
            set(year, month, day, hour, 0)
        }.timeInMillis

    private fun tx(
        title: String,
        time: Long,
        recurring: Boolean = true,
        income: Boolean = false,
        category: String = "Entertainment",
        amount: Double = 10.0
    ) = Transaction(
        id = 0, title = title, category = category, amount = amount,
        isIncome = income, timestamp = time, isRecurring = recurring
    )

    private fun upcoming(vararg list: Transaction) =
        RecurringPayment.fromTransactions(list.toList(), now, tz)

    @Test
    fun `next due is one month after the last payment`() {
        val result = upcoming(tx("Netflix", date(2026, Calendar.NOVEMBER, 2)))
        assertEquals(1, result.size)
        assertEquals(date(2026, Calendar.DECEMBER, 2, hour = 0), result[0].nextDue)
        assertEquals(8, result[0].daysUntil)
        assertFalse(result[0].isUrgent)
    }

    @Test
    fun `uses the latest payment when it repeats`() {
        val result = upcoming(
            tx("Netflix", date(2026, Calendar.SEPTEMBER, 26), amount = 9.0),
            tx("netflix ", date(2026, Calendar.OCTOBER, 26), amount = 11.0)
        )
        assertEquals(1, result.size)
        assertEquals(11.0, result[0].amount, 0.0)
        assertEquals(2, result[0].daysUntil) // Nov 26
        assertTrue(result[0].isUrgent)
    }

    @Test
    fun `missed payment is overdue`() {
        val result = upcoming(tx("Rent", date(2026, Calendar.OCTOBER, 14), category = "Housing"))
        assertEquals(-10, result[0].daysUntil) // was due Nov 14
        assertTrue(result[0].isUrgent)
    }

    @Test
    fun `payment not repeated for over two months is dropped`() {
        assertTrue(upcoming(tx("Old gym", date(2026, Calendar.AUGUST, 1))).isEmpty())
    }

    @Test
    fun `income and one-off expenses are ignored`() {
        val result = upcoming(
            tx("Salary", date(2026, Calendar.NOVEMBER, 1), income = true),
            tx("Coffee", date(2026, Calendar.NOVEMBER, 20), recurring = false)
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `same title in different categories are separate payments`() {
        val result = upcoming(
            tx("Apple", date(2026, Calendar.NOVEMBER, 10), category = "Entertainment"),
            tx("Apple", date(2026, Calendar.NOVEMBER, 12), category = "Shopping")
        )
        assertEquals(2, result.size)
    }

    @Test
    fun `sorted by nearest due date`() {
        val result = upcoming(
            tx("Internet", date(2026, Calendar.NOVEMBER, 20)),
            tx("Spotify", date(2026, Calendar.OCTOBER, 25)),
            tx("Gym", date(2026, Calendar.NOVEMBER, 1))
        )
        assertEquals(listOf("Spotify", "Gym", "Internet"), result.map { it.title })
    }

    @Test
    fun `month end rolls to the last day of a shorter month`() {
        val jan31 = date(2027, Calendar.JANUARY, 31)
        val result = RecurringPayment.fromTransactions(
            listOf(tx("Insurance", jan31)),
            now = date(2027, Calendar.FEBRUARY, 10),
            timeZone = tz
        )
        assertEquals(date(2027, Calendar.FEBRUARY, 28, hour = 0), result[0].nextDue)
    }
}
