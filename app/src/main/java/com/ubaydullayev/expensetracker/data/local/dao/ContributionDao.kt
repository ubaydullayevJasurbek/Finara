package com.ubaydullayev.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ubaydullayev.expensetracker.data.local.entity.ContributionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContributionDao {

    @Insert
    suspend fun insert(contribution: ContributionEntity)


    @Query("SELECT * FROM goal_contributions WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getContributionsSince(sinceTimestamp: Long): Flow<List<ContributionEntity>>
}