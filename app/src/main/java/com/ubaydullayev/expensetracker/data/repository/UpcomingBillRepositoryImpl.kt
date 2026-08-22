package com.ubaydullayev.expensetracker.data.repository

import com.ubaydullayev.expensetracker.data.local.dao.UpcomingBillDao
import com.ubaydullayev.expensetracker.data.local.mapper.toDomainModel
import com.ubaydullayev.expensetracker.data.local.mapper.toEntity
import com.ubaydullayev.expensetracker.domain.model.UpcomingBill
import com.ubaydullayev.expensetracker.domain.repository.UpcomingBillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UpcomingBillRepositoryImpl(private val dao: UpcomingBillDao) : UpcomingBillRepository {
    override fun getAllBills(): Flow<List<UpcomingBill>> {

        return dao.getAllBills().map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }

    override suspend fun addBill(bill: UpcomingBill) {

        dao.insert(bill.toEntity())
    }

    override suspend fun deleteBill(bill: UpcomingBill) {
        dao.delete(bill.toEntity())
    }
}