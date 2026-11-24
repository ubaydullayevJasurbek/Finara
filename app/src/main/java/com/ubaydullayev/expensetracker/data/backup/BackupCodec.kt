package com.ubaydullayev.expensetracker.data.backup

import com.ubaydullayev.expensetracker.data.local.entity.BudgetEntity
import com.ubaydullayev.expensetracker.data.local.entity.ContributionEntity
import com.ubaydullayev.expensetracker.data.local.entity.CustomCategoryEntity
import com.ubaydullayev.expensetracker.data.local.entity.NotificationEntity
import com.ubaydullayev.expensetracker.data.local.entity.SavingGoalEntity
import com.ubaydullayev.expensetracker.data.local.entity.TransactionEntity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Zaxira fayli formati (JSON). Odam o'qiy oladigan, Android'ning o'z org.json kutubxonasi bilan.
 *
 * {
 *   "app": "finara",
 *   "formatVersion": 1,
 *   "createdAt": 1795400000000,
 *   "currency": "USD",
 *   "transactions": [ {...} ], "savingGoals": [...], "budgets": [...],
 *   "customCategories": [...], "contributions": [...], "notifications": [...]
 * }
 *
 * Format o'zgarsa FORMAT_VERSION oshiriladi; eski fayllarni o'qish saqlanib qolishi kerak.
 */
object BackupCodec {

    const val APP_ID = "finara"
    const val FORMAT_VERSION = 1

    fun encode(data: BackupData): String = JSONObject().apply {
        put("app", APP_ID)
        put("formatVersion", FORMAT_VERSION)
        put("createdAt", data.createdAt)
        put("currency", data.currencyCode)
        put("transactions", array(data.transactions) { it.toJson() })
        put("savingGoals", array(data.savingGoals) { it.toJson() })
        put("budgets", array(data.budgets) { it.toJson() })
        put("customCategories", array(data.customCategories) { it.toJson() })
        put("contributions", array(data.contributions) { it.toJson() })
        put("notifications", array(data.notifications) { it.toJson() })
    }.toString(2)

    /** @throws BackupFormatException fayl Finara zaxirasi bo'lmasa yoki buzilgan bo'lsa. */
    fun decode(text: String): BackupData {
        val root = try {
            JSONObject(text)
        } catch (e: JSONException) {
            throw BackupFormatException("Not a JSON file", e)
        }

        if (root.optString("app") != APP_ID) throw BackupFormatException("Not a Finara backup")
        val version = root.optInt("formatVersion", -1)
        if (version < 1) throw BackupFormatException("Missing format version")
        if (version > FORMAT_VERSION) throw BackupFormatException("Backup is from a newer app version")

        return try {
            BackupData(
                createdAt = root.optLong("createdAt", 0L),
                currencyCode = root.optString("currency", "USD").ifBlank { "USD" },
                transactions = root.list("transactions") { it.toTransaction() },
                savingGoals = root.list("savingGoals") { it.toSavingGoal() },
                budgets = root.list("budgets") { it.toBudget() },
                customCategories = root.list("customCategories") { it.toCustomCategory() },
                contributions = root.list("contributions") { it.toContribution() },
                notifications = root.list("notifications") { it.toNotification() }
            )
        } catch (e: JSONException) {
            throw BackupFormatException("Damaged backup", e)
        }
    }

    // ---------- yordamchilar ----------

    private fun <T> array(items: List<T>, toJson: (T) -> JSONObject): JSONArray =
        JSONArray().apply { items.forEach { put(toJson(it)) } }

    /** Majburiy maydon yo'q bo'lsa JSONException - butun fayl "buzilgan" deb rad etiladi. */
    private fun <T> JSONObject.list(key: String, parse: (JSONObject) -> T): List<T> {
        val array = optJSONArray(key) ?: return emptyList()
        return (0 until array.length()).map { parse(array.getJSONObject(it)) }
    }

    // ---------- transactions ----------

    private fun TransactionEntity.toJson() = JSONObject().apply {
        put("id", id); put("title", title); put("category", category); put("amount", amount)
        put("isIncome", isIncome); put("timestamp", timestamp); put("isRecurring", isRecurring)
        put("hasAttachment", hasAttachment); put("note", note)
    }

    private fun JSONObject.toTransaction() = TransactionEntity(
        id = getLong("id"),
        title = getString("title"),
        category = getString("category"),
        amount = getDouble("amount"),
        isIncome = getBoolean("isIncome"),
        timestamp = getLong("timestamp"),
        isRecurring = optBoolean("isRecurring", false),
        hasAttachment = optBoolean("hasAttachment", false),
        note = optString("note", "")
    )

    // ---------- saving goals ----------

    private fun SavingGoalEntity.toJson() = JSONObject().apply {
        put("id", id); put("title", title); put("category", category)
        put("estimatedDate", estimatedDate); put("priority", priority); put("targetDate", targetDate)
        put("currentAmount", currentAmount); put("targetAmount", targetAmount); put("status", status)
    }

    private fun JSONObject.toSavingGoal() = SavingGoalEntity(
        id = getLong("id"),
        title = getString("title"),
        category = optString("category", ""),
        estimatedDate = optString("estimatedDate", ""),
        priority = optString("priority", ""),
        targetDate = optString("targetDate", ""),
        currentAmount = getDouble("currentAmount"),
        targetAmount = getDouble("targetAmount"),
        status = optString("status", "ACTIVE")
    )

    // ---------- budgets ----------

    private fun BudgetEntity.toJson() = JSONObject().apply {
        put("id", id); put("title", title); put("category", category)
        put("spentAmount", spentAmount); put("limitAmount", limitAmount)
    }

    private fun JSONObject.toBudget() = BudgetEntity(
        id = getLong("id"),
        title = getString("title"),
        category = getString("category"),
        spentAmount = optDouble("spentAmount", 0.0),
        limitAmount = getDouble("limitAmount")
    )

    // ---------- custom categories ----------

    private fun CustomCategoryEntity.toJson() = JSONObject().apply {
        put("id", id); put("name", name)
    }

    private fun JSONObject.toCustomCategory() = CustomCategoryEntity(
        id = getLong("id"),
        name = getString("name")
    )

    // ---------- contributions ----------

    private fun ContributionEntity.toJson() = JSONObject().apply {
        put("id", id); put("goalId", goalId); put("amount", amount); put("timestamp", timestamp)
    }

    private fun JSONObject.toContribution() = ContributionEntity(
        id = getLong("id"),
        goalId = getLong("goalId"),
        amount = getDouble("amount"),
        timestamp = getLong("timestamp")
    )

    // ---------- notifications ----------

    private fun NotificationEntity.toJson() = JSONObject().apply {
        put("id", id); put("title", title); put("message", message); put("type", type)
        put("timestamp", timestamp); put("isRead", isRead)
    }

    private fun JSONObject.toNotification() = NotificationEntity(
        id = getLong("id"),
        title = getString("title"),
        message = optString("message", ""),
        type = getString("type"),
        timestamp = getLong("timestamp"),
        isRead = optBoolean("isRead", false)
    )
}
