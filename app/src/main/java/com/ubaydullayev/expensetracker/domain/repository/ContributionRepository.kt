package com.ubaydullayev.expensetracker.domain.repository

import com.ubaydullayev.expensetracker.domain.model.Contribution
import kotlinx.coroutines.flow.Flow

interface ContributionRepository {

    fun getContributionsSince(sinceTimestamp:Long): Flow<List<Contribution>>
    suspend fun addContribution(contribution: Contribution)
}