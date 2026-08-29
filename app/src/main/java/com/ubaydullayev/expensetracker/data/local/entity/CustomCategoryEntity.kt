package com.ubaydullayev.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "custom_category",
    indices = [Index(value = ["name"], unique = true)]
)
data class CustomCategoryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)