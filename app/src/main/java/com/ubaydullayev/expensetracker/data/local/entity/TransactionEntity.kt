package com.ubaydullayev.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)

    val id: Long = 0,

    val title: String,
    val category: String,
    val dateTime: String,
    val amount: Double,
    val isIncome: Boolean,
)