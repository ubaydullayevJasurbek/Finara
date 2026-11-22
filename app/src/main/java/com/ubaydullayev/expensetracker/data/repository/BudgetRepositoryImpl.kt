package com.ubaydullayev.expensetracker.data.repository

import com.ubaydullayev.expensetracker.data.local.dao.BudgetDao
import com.ubaydullayev.expensetracker.data.local.mapper.toDomainModel
import com.ubaydullayev.expensetracker.data.local.mapper.toEntity
import com.ubaydullayev.expensetracker.domain.model.Budget
import com.ubaydullayev.expensetracker.domain.repository.BudgetRepository
import com.ubaydullayev.expensetracker.utils.BudgetCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetRepositoryImpl(private val dao: BudgetDao) : BudgetRepository {


    /**
     * spentAmount joriy oy tranzaksiyalaridan hisoblanadi (bazadagi spentAmount ustuni ishlatilmaydi).
     * Eslatma: oy chegarasi Flow yaratilganda olinadi; ekran qayta ochilganda yangilanadi.
     */
    override fun getAllBudgets(): Flow<List<Budget>> {
        val (monthStart, monthEnd) = BudgetCalculator.monthRange()
        return dao.getBudgetsWithSpending(monthStart, monthEnd).map { entities ->
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