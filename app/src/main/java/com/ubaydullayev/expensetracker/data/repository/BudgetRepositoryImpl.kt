package com.ubaydullayev.expensetracker.data.repository

import com.ubaydullayev.expensetracker.data.local.dao.BudgetDao
import com.ubaydullayev.expensetracker.data.local.mapper.toDomainModel
import com.ubaydullayev.expensetracker.data.local.mapper.toEntity
import com.ubaydullayev.expensetracker.domain.model.Budget
import com.ubaydullayev.expensetracker.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetRepositoryImpl(private val dao: BudgetDao) : BudgetRepository {


    override fun getAllBudgets(): Flow<List<Budget>> {
        return dao.getAllBudgets().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun addBudget(budget: Budget) {

        dao.insert(budget.toEntity())
    }

    override suspend fun updateBudget(budget: Budget) {
        dao.update(budget.toEntity())
    }

    override suspend fun deleteBudget(budget: Budget) {
        dao.delete(budget.toEntity())
    }

}