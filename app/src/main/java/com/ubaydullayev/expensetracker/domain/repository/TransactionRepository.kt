package com.ubaydullayev.expensetracker.domain.repository

import com.ubaydullayev.expensetracker.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun getAllTransactions(): Flow<List<Transaction>>
    fun getRecentTransactions(limit: Int): Flow<List<Transaction>>
    suspend fun addTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)


    fun getTotalBalance(): Flow<Double>
    fun getMonthlyIncome(monthStart: Long): Flow<Double>
    fun getMonthlyExpense(monthStart: Long): Flow<Double>
    fun getIncomeBetween(start: Long, end: Long): Flow<Double>
    fun getExpenseBetween(start: Long, end: Long): Flow<Double>
}