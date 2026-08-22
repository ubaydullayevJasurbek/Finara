package com.ubaydullayev.expensetracker.data.repository

import com.ubaydullayev.expensetracker.data.local.dao.SavingGoalDao
import com.ubaydullayev.expensetracker.data.local.mapper.toDomainModel
import com.ubaydullayev.expensetracker.data.local.mapper.toEntity
import com.ubaydullayev.expensetracker.domain.model.SavingGoal
import com.ubaydullayev.expensetracker.domain.repository.SavingGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavingGoalRepositoryImpl(private val dao: SavingGoalDao) : SavingGoalRepository {
    override fun getAllGoals(): Flow<List<SavingGoal>> {

        return dao.getAllGoals().map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }

    override suspend fun addGoal(goal: SavingGoal) {
        dao.insert(goal.toEntity())
    }

    override suspend fun updateGoal(goal: SavingGoal) {
        dao.update(goal.toEntity())
    }

    override suspend fun deleteGoal(goal: SavingGoal) {
        dao.delete(goal.toEntity())
    }
}