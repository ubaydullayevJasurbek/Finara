package com.ubaydullayev.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ubaydullayev.expensetracker.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert
    suspend fun insert(budget: BudgetEntity)

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets ORDER BY id DESC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    /**
     * Budjetlar + shu davrdagi haqiqiy xarajat (spentAmount ustunidagi saqlangan qiymat EMAS).
     * spentAmount = shu kategoriyadagi xarajat tranzaksiyalari yig'indisi [start, end).
     * Room ikkala jadvalni kuzatadi: tranzaksiya qo'shilsa/o'chirilsa natija qayta keladi.
     */
    @Query(
        """
        SELECT b.id, b.title, b.category, b.limitAmount,
               COALESCE(SUM(t.amount), 0) AS spentAmount
        FROM budgets b
        LEFT JOIN transactions t
            ON t.category = b.category COLLATE NOCASE
            AND t.isIncome = 0
            AND t.timestamp >= :start AND t.timestamp < :end
        GROUP BY b.id
        ORDER BY b.id DESC
        """
    )
    fun getBudgetsWithSpending(start: Long, end: Long): Flow<List<BudgetEntity>>
}