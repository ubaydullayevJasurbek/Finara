package com.ubaydullayev.expensetracker.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ubaydullayev.expensetracker.data.local.dao.SavingGoalDao
import com.ubaydullayev.expensetracker.data.local.dao.TransactionDao
import com.ubaydullayev.expensetracker.data.local.dao.UpcomingBillDao
import com.ubaydullayev.expensetracker.data.local.entity.SavingGoalEntity
import com.ubaydullayev.expensetracker.data.local.entity.TransactionEntity
import com.ubaydullayev.expensetracker.data.local.entity.UpcomingBillEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Database(
    entities = [
        TransactionEntity::class,
        UpcomingBillEntity::class,
        SavingGoalEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun upcomingBillDao(): UpcomingBillDao
    abstract fun savingGoalDao(): SavingGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val databaseScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }


    }
}