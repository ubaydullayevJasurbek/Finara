package com.ubaydullayev.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saving_goals")
data class SavingGoalEntity(
    @PrimaryKey(autoGenerate = true)

    val id: Long = 0,
    val title: String,
    val category: String,
    val estimatedDate: String,
    val priority: String,
    val targetDate: String,
    val currentAmount: Double,
    val targetAmount: Double
)