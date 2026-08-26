package com.ubaydullayev.expensetracker.presentation.transaction

import com.ubaydullayev.expensetracker.domain.model.Transaction

sealed class TransactionListItem {

    data class DateHeader(
        val label: String,      // "Today", "Yesterday", "Aug 20"
        val totalAmount: Double // shu kunning jami summasi (income - expense)
    ) : TransactionListItem()

    data class TransactionRow(
        val transaction: Transaction
    ) : TransactionListItem()
}