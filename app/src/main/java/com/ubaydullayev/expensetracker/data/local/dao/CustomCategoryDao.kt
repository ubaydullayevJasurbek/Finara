package com.ubaydullayev.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ubaydullayev.expensetracker.data.local.entity.CustomCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomCategoryDao {

    @Insert
    suspend fun insert(category: CustomCategoryEntity)

    @Query("SELECT * FROM custom_category ORDER BY id DESC")
    fun getAllCustomCategories(): Flow<List<CustomCategoryEntity>>
}