package com.ubaydullayev.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "custom_category")
data class CustomCategoryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)