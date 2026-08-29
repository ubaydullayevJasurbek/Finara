package com.ubaydullayev.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)

    val id: Long = 0,

    val title: String,
    val category: String,
    val amount: Double,
    val isIncome: Boolean,
    val timestamp: Long,
    val isRecurring: Boolean = false,
    val hasAttachment: Boolean = false,
    val note: String = ""
)