package com.ubaydullayev.expensetracker.data.backup

import com.ubaydullayev.expensetracker.data.local.entity.BudgetEntity
import com.ubaydullayev.expensetracker.data.local.entity.ContributionEntity
import com.ubaydullayev.expensetracker.data.local.entity.CustomCategoryEntity
import com.ubaydullayev.expensetracker.data.local.entity.NotificationEntity
import com.ubaydullayev.expensetracker.data.local.entity.SavingGoalEntity
import com.ubaydullayev.expensetracker.data.local.entity.TransactionEntity

/**
 * Zaxira nusxaning to'liq mazmuni: bazadagi barcha jadvallar + valyuta sozlamasi.
 * ID'lar o'zgarmay saqlanadi, chunki hissalar (contributions) maqsadga goalId orqali bog'langan.
 */
data class BackupData(
    val createdAt: Long,
    val currencyCode: String,
    val transactions: List<TransactionEntity>,
    val savingGoals: List<SavingGoalEntity>,
    val budgets: List<BudgetEntity>,
    val customCategories: List<CustomCategoryEntity>,
    val contributions: List<ContributionEntity>,
    val notifications: List<NotificationEntity>
) {
    /** Tiklashdan oldin foydalanuvchiga ko'rsatiladigan qisqa ma'lumot. */
    val summary: BackupSummary
        get() = BackupSummary(
            createdAt = createdAt,
            transactions = transactions.size,
            savingGoals = savingGoals.size,
            budgets = budgets.size
        )
}

data class BackupSummary(
    val createdAt: Long,
    val transactions: Int,
    val savingGoals: Int,
    val budgets: Int
)

/** Fayl Finara zaxirasi emas, buzilgan yoki ilovaning yangiroq versiyasida yaratilgan. */
class BackupFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)
