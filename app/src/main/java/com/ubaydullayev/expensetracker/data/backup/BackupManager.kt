package com.ubaydullayev.expensetracker.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.ubaydullayev.expensetracker.data.local.database.AppDatabase
import com.ubaydullayev.expensetracker.data.local.prefs.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Zaxira nusxa yaratish va undan tiklash.
 * Fayl joyini foydalanuvchi o'zi tanlaydi (telefon, Google Drive va h.k.), shuning uchun
 * hech qanday ruxsat (permission) va server kerak emas.
 */
class BackupManager(context: Context) {

    private val appContext = context.applicationContext
    private val db = AppDatabase.getInstance(appContext)
    private val prefs = UserPreferences.getInstance(appContext)

    /** Barcha ma'lumotlarni [uri] fayliga yozadi. */
    suspend fun writeBackup(uri: Uri) = withContext(Dispatchers.IO) {
        val data = BackupData(
            createdAt = System.currentTimeMillis(),
            currencyCode = prefs.currencyCode.value,
            transactions = db.transactionDao().getAllForBackup(),
            savingGoals = db.savingGoalDao().getAllForBackup(),
            budgets = db.budgetDao().getAllForBackup(),
            customCategories = db.customCategoryDao().getAllForBackup(),
            contributions = db.contributionDao().getAllForBackup(),
            notifications = db.notificationDao().getAllForBackup()
        )
        val stream = appContext.contentResolver.openOutputStream(uri, "w")
            ?: error("Cannot open file for writing")
        stream.bufferedWriter(Charsets.UTF_8).use { it.write(BackupCodec.encode(data)) }
    }

    /**
     * Faylni o'qib tekshiradi, lekin bazaga hali yozmaydi:
     * foydalanuvchi avval nima tiklanishini ko'rib tasdiqlaydi.
     * @throws BackupFormatException fayl Finara zaxirasi bo'lmasa.
     */
    suspend fun readBackup(uri: Uri): BackupData = withContext(Dispatchers.IO) {
        val stream = appContext.contentResolver.openInputStream(uri)
            ?: error("Cannot open file for reading")
        val text = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        BackupCodec.decode(text)
    }

    /**
     * Qurilmadagi barcha ma'lumotlarni zaxiradagisi bilan almashtiradi.
     * Bitta tranzaksiyada: biror narsa xato bo'lsa, eski ma'lumotlar tegmasdan qoladi.
     */
    suspend fun restore(data: BackupData) {
        db.withTransaction {
            db.contributionDao().deleteAll()
            db.transactionDao().deleteAll()
            db.savingGoalDao().deleteAll()
            db.budgetDao().deleteAll()
            db.customCategoryDao().deleteAll()
            db.notificationDao().deleteAll()

            db.transactionDao().insertAll(data.transactions)
            db.savingGoalDao().insertAll(data.savingGoals)
            db.budgetDao().insertAll(data.budgets)
            db.customCategoryDao().insertAll(data.customCategories)
            db.contributionDao().insertAll(data.contributions)
            db.notificationDao().insertAll(data.notifications)
        }
        prefs.setCurrencyCode(data.currencyCode)
    }
}
