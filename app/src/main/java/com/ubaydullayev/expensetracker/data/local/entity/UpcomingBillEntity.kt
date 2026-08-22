package com.ubaydullayev.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upcoming_bills")
data class UpcomingBillEntity(

    @PrimaryKey(autoGenerate = true)

    val id: Long = 0,
    val title: String,
    val category: String,
    val frequency: String,
    val price: Double,
    val dueText: String,
    val isUrgent: Boolean
)