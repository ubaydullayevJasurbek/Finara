package com.ubaydullayev.expensetracker.domain.repository

import com.ubaydullayev.expensetracker.domain.model.GoalStatus
import com.ubaydullayev.expensetracker.domain.model.SavingGoal
import kotlinx.coroutines.flow.Flow

interface SavingGoalRepository {

    fun getAllGoals(): Flow<List<SavingGoal>>

    suspend fun addGoal(goal: SavingGoal)
    suspend fun updateGoal(goal: SavingGoal)
    suspend fun deleteGoal(goal: SavingGoal)


    fun getActiveGoals(): Flow<List<SavingGoal>>
    suspend fun getGoalById(id: Long): SavingGoal?
    suspend fun addToCurrentAmount(goalId: Long, amount: Double)
    suspend fun updateStatus(goalId: Long, status: GoalStatus)
}