package com.ubaydullayev.expensetracker.domain.model

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.roundToInt

/**
 * Takrorlanuvchi to'lov (obuna, ijara, internet...). Alohida jadvalda saqlanmaydi:
 * foydalanuvchi "Recurring" deb belgilagan xarajatlardan hisoblanadi.
 */
data class RecurringPayment(
    val title: String,
    val category: String,
    val amount: Double,
    val lastPaid: Long,
    val nextDue: Long,
    /** Bugundan keyingi to'lovgacha kunlar. Manfiy - muddati o'tgan. */
    val daysUntil: Int
) {
    /** Yaqin (3 kun ichida) yoki muddati o'tgan. */
    val isUrgent: Boolean get() = daysUntil <= URGENT_DAYS

    companion object {
        const val URGENT_DAYS = 3

        /**
         * Oxirgi to'lovdan shuncha kun o'tgan bo'lsa, obuna to'xtatilgan deb hisoblanadi
         * (bir oy + bir oylik kechikish).
         */
        const val ACTIVE_WINDOW_DAYS = 62

        private const val DAY_MS = 24 * 60 * 60 * 1000L

        /**
         * Har bir takrorlanuvchi xarajat uchun keyingi to'lov sanasi.
         * - Bir xil nom (katta-kichik harf farqisiz) va kategoriya - bitta to'lov.
         * - Oxirgi to'lovdan bir oy keyin - keyingi sana (ilovada chastota hozircha oylik).
         * - Daromadlar va 62 kundan beri takrorlanmagan to'lovlar kirmaydi.
         * Natija eng yaqin sana bo'yicha tartiblangan.
         */
        fun fromTransactions(
            transactions: List<Transaction>,
            now: Long,
            timeZone: TimeZone = TimeZone.getDefault()
        ): List<RecurringPayment> {
            val today = startOfDay(now, timeZone)

            return transactions
                .filter { it.isRecurring && !it.isIncome && it.title.isNotBlank() }
                .groupBy { it.title.trim().lowercase() to it.category }
                .values
                .mapNotNull { occurrences ->
                    val last = occurrences.maxBy { it.timestamp }
                    val lastDay = startOfDay(last.timestamp, timeZone)
                    if (daysBetween(lastDay, today) > ACTIVE_WINDOW_DAYS) return@mapNotNull null

                    val nextDue = Calendar.getInstance(timeZone).apply {
                        timeInMillis = lastDay
                        add(Calendar.MONTH, 1)
                    }.timeInMillis

                    RecurringPayment(
                        title = last.title.trim(),
                        category = last.category,
                        amount = last.amount,
                        lastPaid = last.timestamp,
                        nextDue = nextDue,
                        daysUntil = daysBetween(today, nextDue)
                    )
                }
                .sortedBy { it.daysUntil }
        }

        private fun startOfDay(time: Long, timeZone: TimeZone): Long =
            Calendar.getInstance(timeZone).apply {
                timeInMillis = time
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

        /** Yozgi vaqtga o'tish kunlarida ham to'g'ri: yaxlitlanadi. */
        private fun daysBetween(fromDay: Long, toDay: Long): Int =
            ((toDay - fromDay).toDouble() / DAY_MS).roundToInt()
    }
}
