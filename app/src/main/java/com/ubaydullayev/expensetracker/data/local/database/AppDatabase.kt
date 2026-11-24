package com.ubaydullayev.expensetracker.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ubaydullayev.expensetracker.data.local.dao.BudgetDao
import com.ubaydullayev.expensetracker.data.local.dao.ContributionDao
import com.ubaydullayev.expensetracker.data.local.dao.CustomCategoryDao
import com.ubaydullayev.expensetracker.data.local.dao.NotificationDao
import com.ubaydullayev.expensetracker.data.local.dao.SavingGoalDao
import com.ubaydullayev.expensetracker.data.local.dao.TransactionDao
import com.ubaydullayev.expensetracker.data.local.entity.BudgetEntity
import com.ubaydullayev.expensetracker.data.local.entity.ContributionEntity
import com.ubaydullayev.expensetracker.data.local.entity.CustomCategoryEntity
import com.ubaydullayev.expensetracker.data.local.entity.NotificationEntity
import com.ubaydullayev.expensetracker.data.local.entity.SavingGoalEntity
import com.ubaydullayev.expensetracker.data.local.entity.TransactionEntity

/**
 * Ilovaning lokal bazasi. Yangi foydalanuvchi bo'sh baza bilan boshlaydi:
 * namuna (seed) ma'lumotlar endi yozilmaydi.
 *
 * Versiyalar:
 * 1 - dastlabki sxema (upcoming_bills jadvali bilan).
 * 2 - upcoming_bills olib tashlandi: takrorlanuvchi to'lovlar "Recurring" belgili
 *     tranzaksiyalardan hisoblanadi (RecurringPayment).
 */
@Database(
    entities = [
        TransactionEntity::class,
        SavingGoalEntity::class,
        BudgetEntity::class,
        CustomCategoryEntity::class,
        ContributionEntity::class,
        NotificationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun savingGoalDao(): SavingGoalDao
    abstract fun budgetDao(): BudgetDao
    abstract fun contributionDao(): ContributionDao
    abstract fun notificationDao(): NotificationDao
    abstract fun customCategoryDao(): CustomCategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Foydalanuvchining boshqa ma'lumotlari saqlanadi, faqat eski jadval o'chiriladi. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS upcoming_bills")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
