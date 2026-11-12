package com.ubaydullayev.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String,        // enum nomi "BUDGET" ko'rinishida (SavingGoal.status kabi)
    val timestamp: Long,
    val isRead: Boolean = false
)