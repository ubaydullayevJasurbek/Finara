package com.ubaydullayev.expensetracker.domain.repository

import com.ubaydullayev.expensetracker.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {

    fun getAllBudgets(): Flow<List<Budget>>
    suspend fun addBudget(budget: Budget)
    suspend fun updateBudget(budget: Budget)
    suspend fun deleteBudget(budget: Budget)
}