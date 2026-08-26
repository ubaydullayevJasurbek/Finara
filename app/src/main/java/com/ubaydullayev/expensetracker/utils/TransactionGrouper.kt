package com.ubaydullayev.expensetracker.utils

import com.ubaydullayev.expensetracker.domain.model.Transaction
import com.ubaydullayev.expensetracker.presentation.transaction.TransactionListItem
import java.util.Calendar
import kotlin.collections.iterator

object TransactionGrouper {

    fun groupByDay(transactions: List<Transaction>): List<TransactionListItem> {
        if (transactions.isEmpty()) return emptyList()

        // 1. Avval eng yangi tranzaksiya birinchi bo'lishi kerak (DAO'dan allaqachon shunday keladi)
        val result = mutableListOf<TransactionListItem>()

        // 2. Kun bo'yicha guruhlaymiz — kalit: "label" (masalan "Today")
        val grouped = transactions.groupBy { DateFormatter.getDayLabel(it.timestamp) }

        // 3. Guruhlarni "eng yangi kun birinchi" tartibida chiqaramiz
        //    (groupBy tartibni saqlaydi, chunki transactions allaqachon saralangan)
        for ((label, dayTransactions) in grouped) {
            val dayTotal = dayTransactions.sumOf {
                if (it.isIncome) it.amount else -it.amount
            }

            result.add(TransactionListItem.DateHeader(label, dayTotal))

            dayTransactions.forEach { transaction ->
                result.add(TransactionListItem.TransactionRow(transaction))
            }
        }

        return result
    }
}