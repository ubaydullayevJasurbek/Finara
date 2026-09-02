package com.ubaydullayev.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "goal_contributions")
data class ContributionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val goalId: Long,
    val amount: Double,
    val timestamp: Long

)
