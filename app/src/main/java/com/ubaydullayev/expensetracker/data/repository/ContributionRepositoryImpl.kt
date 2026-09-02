package com.ubaydullayev.expensetracker.data.repository

import com.ubaydullayev.expensetracker.data.local.dao.ContributionDao
import com.ubaydullayev.expensetracker.data.local.mapper.toDomainModel
import com.ubaydullayev.expensetracker.data.local.mapper.toEntity
import com.ubaydullayev.expensetracker.domain.model.Contribution
import com.ubaydullayev.expensetracker.domain.repository.ContributionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContributionRepositoryImpl(private val dao: ContributionDao) : ContributionRepository {


    override fun getContributionsSince(sinceTimestamp: Long): Flow<List<Contribution>> {
        return dao.getContributionsSince(sinceTimestamp).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun addContribution(contribution: Contribution) {
        dao.insert(contribution.toEntity())
    }
}