package com.ubaydullayev.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.ubaydullayev.expensetracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT*FROM transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT*FROM transactions ORDER BY id DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT*FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT COALESCE(SUM(CASE WHEN isIncome = 1 THEN amount ELSE -amount END), 0) FROM transactions")
    fun getTotalBalance(): Flow<Double>

    // Shu oydagi umumiy income
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE isIncome = 1 AND timestamp >= :monthStart")
    fun getMonthlyIncome(monthStart: Long): Flow<Double>

    // Shu oydagi umumiy expense
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE isIncome = 0 AND timestamp >= :monthStart")
    fun getMonthlyExpense(monthStart: Long): Flow<Double>

    // Trend hisoblash uchun — o'tgan oyning income/expense'i
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE isIncome = 1 AND timestamp >= :start AND timestamp < :end")
    fun getIncomeBetween(start: Long, end: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE isIncome = 0 AND timestamp >= :start AND timestamp < :end")
    fun getExpenseBetween(start: Long, end: Long): Flow<Double>

}

