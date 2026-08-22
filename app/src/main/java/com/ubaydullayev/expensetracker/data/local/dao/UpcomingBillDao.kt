package com.ubaydullayev.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.ubaydullayev.expensetracker.data.local.entity.UpcomingBillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UpcomingBillDao {

    @Insert
    suspend fun insert(bill: UpcomingBillEntity)

    @Delete
    suspend fun delete(bill: UpcomingBillEntity)

    @Query("SELECT * FROM upcoming_bills ORDER BY id ASC")
    fun getAllBills(): Flow<List<UpcomingBillEntity>>
}