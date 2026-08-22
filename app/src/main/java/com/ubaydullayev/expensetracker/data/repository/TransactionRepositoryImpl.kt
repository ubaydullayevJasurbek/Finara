package com.ubaydullayev.expensetracker.data.repository

import com.ubaydullayev.expensetracker.data.local.dao.TransactionDao
import com.ubaydullayev.expensetracker.data.local.mapper.toDomainModel
import com.ubaydullayev.expensetracker.data.local.mapper.toEntity
import com.ubaydullayev.expensetracker.domain.model.Transaction
import com.ubaydullayev.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(private val dao: TransactionDao) : TransactionRepository {


    override fun getAllTransactions(): Flow<List<Transaction>> {

        return dao.getAllTransactions().map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }

    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> {

        return dao.getRecentTransactions(limit).map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }

    override suspend fun addTransaction(transaction: Transaction) {

        dao.insert(transaction.toEntity())
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        dao.delete(transaction.toEntity())
    }
}