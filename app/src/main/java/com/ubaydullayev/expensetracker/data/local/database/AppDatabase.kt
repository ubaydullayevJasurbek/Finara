package com.ubaydullayev.expensetracker.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ubaydullayev.expensetracker.data.local.dao.BudgetDao
import com.ubaydullayev.expensetracker.data.local.dao.SavingGoalDao
import com.ubaydullayev.expensetracker.data.local.dao.TransactionDao
import com.ubaydullayev.expensetracker.data.local.dao.UpcomingBillDao
import com.ubaydullayev.expensetracker.data.local.entity.BudgetEntity
import com.ubaydullayev.expensetracker.data.local.entity.SavingGoalEntity
import com.ubaydullayev.expensetracker.data.local.entity.TransactionEntity
import com.ubaydullayev.expensetracker.data.local.entity.UpcomingBillEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        UpcomingBillEntity::class,
        SavingGoalEntity::class,
        BudgetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun upcomingBillDao(): UpcomingBillDao
    abstract fun savingGoalDao(): SavingGoalDao

    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val databaseScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                lateinit var instanceRef: AppDatabase

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_db"
                )
                    .addCallback(object :
                        Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            databaseScope.launch {
                                seedBudgets(instanceRef.budgetDao())
                                seedTransactions(instanceRef.transactionDao())
                                seedUpcomingBills(instanceRef.upcomingBillDao())
                                seedSavingGoals(instanceRef.savingGoalDao())
                            }
                        }
                    })
                    .build()

                instanceRef = instance
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedBudgets(dao: BudgetDao) {
            dao.insert(
                BudgetEntity(
                    title = "Housing",
                    category = "Housing",
                    spentAmount = 1200.0,
                    limitAmount = 1500.0
                )
            )
            dao.insert(
                BudgetEntity(
                    title = "Transport",
                    category = "Transport",
                    spentAmount = 420.0,
                    limitAmount = 450.0
                )
            )
        }

        private suspend fun seedTransactions(dao: TransactionDao) {
            val now = System.currentTimeMillis()
            val oneDayMs = 24 * 60 * 60 * 1000L

            dao.insert(
                TransactionEntity(
                    title = "Salary", category = "Income", timestamp = now,
                    amount = 3200.00, isIncome = true, isRecurring = true
                )
            )
            dao.insert(
                TransactionEntity(
                    title = "Grocery Store",
                    category = "Groceries",
                    timestamp = now - 5 * 60 * 60 * 1000,
                    amount = 54.20,
                    isIncome = false
                )
            )
            dao.insert(
                TransactionEntity(
                    title = "Netflix Premium",
                    category = "Entertainment",
                    timestamp = now - oneDayMs,
                    amount = 22.99,
                    isIncome = false,
                    isRecurring = true
                )
            )
            dao.insert(
                TransactionEntity(
                    title = "Flight Ticket",
                    category = "Travel",
                    timestamp = now - oneDayMs - 2 * 60 * 60 * 1000,
                    amount = 189.00,
                    isIncome = false
                )
            )
        }

        private suspend fun seedUpcomingBills(dao: UpcomingBillDao) {
            dao.insert(
                UpcomingBillEntity(
                    title = "Netflix Premium",
                    category = "Entertainment",
                    frequency = "Monthly",
                    price = 22.99,
                    dueText = "Due in 2d",
                    isUrgent = true
                )
            )
            dao.insert(
                UpcomingBillEntity(
                    title = "Car Insurance",
                    category = "Insurance",
                    frequency = "Monthly",
                    price = 140.00,
                    dueText = "Due in 5d",
                    isUrgent = false
                )
            )
            dao.insert(
                UpcomingBillEntity(
                    title = "Adobe Creative",
                    category = "Software",
                    frequency = "Monthly",
                    price = 54.99,
                    dueText = "Due in 9d",
                    isUrgent = false
                )
            )
        }

        private suspend fun seedSavingGoals(dao: SavingGoalDao) {
            dao.insert(
                SavingGoalEntity(
                    title = "Japan Trip 2026",
                    category = "Travel",   // "Groceries" -> "Travel" TUZATILDI
                    estimatedDate = "Est. Nov 2026",
                    priority = "HIGH",
                    targetDate = "Dec 2026",
                    currentAmount = 4080.0,
                    targetAmount = 6000.0
                )
            )
        }
    }
}