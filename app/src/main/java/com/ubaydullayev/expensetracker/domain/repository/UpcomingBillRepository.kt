package com.ubaydullayev.expensetracker.domain.repository

import com.ubaydullayev.expensetracker.domain.model.UpcomingBill
import kotlinx.coroutines.flow.Flow

interface UpcomingBillRepository {

    fun getAllBills(): Flow<List<UpcomingBill>>
    suspend fun addBill(bill: UpcomingBill)
    suspend fun deleteBill(bill: UpcomingBill)
}