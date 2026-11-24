package com.ubaydullayev.expensetracker.data.backup

import com.ubaydullayev.expensetracker.data.local.entity.BudgetEntity
import com.ubaydullayev.expensetracker.data.local.entity.ContributionEntity
import com.ubaydullayev.expensetracker.data.local.entity.CustomCategoryEntity
import com.ubaydullayev.expensetracker.data.local.entity.NotificationEntity
import com.ubaydullayev.expensetracker.data.local.entity.SavingGoalEntity
import com.ubaydullayev.expensetracker.data.local.entity.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BackupCodecTest {

    private val sample = BackupData(
        createdAt = 1_795_400_000_000,
        currencyCode = "UZS",
        transactions = listOf(
            TransactionEntity(
                id = 7, title = "Netflix, \"Premium\"", category = "Entertainment", amount = 22.99,
                isIncome = false, timestamp = 1_795_000_000_000, isRecurring = true,
                hasAttachment = false, note = "Line 1\nLine 2 — ünïcode"
            ),
            TransactionEntity(
                id = 8, title = "Salary", category = "Salary", amount = 5240.0,
                isIncome = true, timestamp = 1_794_000_000_000
            )
        ),
        savingGoals = listOf(
            SavingGoalEntity(
                id = 3, title = "Japan Trip", category = "Travel", estimatedDate = "Est. Nov 2026",
                priority = "HIGH", targetDate = "Dec 2026", currentAmount = 4080.0,
                targetAmount = 6000.0, status = "ACTIVE"
            )
        ),
        budgets = listOf(
            BudgetEntity(id = 2, title = "Food", category = "Food & Drinks", spentAmount = 0.0, limitAmount = 400.0)
        ),
        customCategories = listOf(CustomCategoryEntity(id = 1, name = "Pets")),
        contributions = listOf(ContributionEntity(id = 11, goalId = 3, amount = 240.0, timestamp = 1_795_100_000_000)),
        notifications = listOf(
            NotificationEntity(id = 5, title = "Budget", message = "92%", type = "BUDGET", timestamp = 1_795_200_000_000, isRead = true)
        )
    )

    @Test
    fun `round trip keeps every field and id`() {
        val decoded = BackupCodec.decode(BackupCodec.encode(sample))
        assertEquals(sample, decoded)
    }

    @Test
    fun `summary counts the main records`() {
        val summary = sample.summary
        assertEquals(2, summary.transactions)
        assertEquals(1, summary.savingGoals)
        assertEquals(1, summary.budgets)
    }

    @Test
    fun `missing optional fields get defaults`() {
        val json = """
            {"app":"finara","formatVersion":1,
             "transactions":[{"id":1,"title":"Coffee","category":"Food","amount":3.5,"isIncome":false,"timestamp":10}]}
        """.trimIndent()
        val data = BackupCodec.decode(json)
        assertEquals("USD", data.currencyCode)
        assertEquals(false, data.transactions[0].isRecurring)
        assertEquals("", data.transactions[0].note)
        assertTrue(data.savingGoals.isEmpty())
    }

    @Test
    fun `rejects a file that is not json`() = assertRejected("hello")

    @Test
    fun `rejects json from another app`() = assertRejected("""{"app":"other","formatVersion":1}""")

    @Test
    fun `rejects a backup from a newer app version`() =
        assertRejected("""{"app":"finara","formatVersion":99}""")

    @Test
    fun `rejects a damaged record`() = assertRejected(
        """{"app":"finara","formatVersion":1,"transactions":[{"id":1,"title":"No amount"}]}"""
    )

    private fun assertRejected(text: String) {
        try {
            BackupCodec.decode(text)
            fail("Expected BackupFormatException")
        } catch (expected: BackupFormatException) {
            // ok
        }
    }
}
