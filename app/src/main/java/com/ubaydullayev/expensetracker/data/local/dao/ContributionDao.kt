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

    // ---- Backup / Restore (BackupManager) ----

    @Query("SELECT * FROM goal_contributions")
    suspend fun getAllForBackup(): List<ContributionEntity>

    @Query("DELETE FROM goal_contributions")
    suspend fun deleteAll()

    @Insert
    suspend fun insertAll(items: List<ContributionEntity>)
}
