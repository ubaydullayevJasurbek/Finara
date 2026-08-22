package com.ubaydullayev.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ubaydullayev.expensetracker.data.local.entity.SavingGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingGoalDao {


    @Insert
    suspend fun insert(goal: SavingGoalEntity)

    @Update
    suspend fun update(goal: SavingGoalEntity)

    @Delete
    suspend fun delete(goal: SavingGoalEntity)

    @Query("SELECT * FROM saving_goals ORDER BY id ASC")
    fun getAllGoals(): Flow<List<SavingGoalEntity>>
}