package com.ubaydullayev.expensetracker.data.local.mapper

import com.ubaydullayev.expensetracker.data.local.entity.BudgetEntity
import com.ubaydullayev.expensetracker.data.local.entity.ContributionEntity
import com.ubaydullayev.expensetracker.data.local.entity.SavingGoalEntity
import com.ubaydullayev.expensetracker.data.local.entity.TransactionEntity
import com.ubaydullayev.expensetracker.data.local.entity.UpcomingBillEntity
import com.ubaydullayev.expensetracker.domain.model.Budget
import com.ubaydullayev.expensetracker.domain.model.Contribution
import com.ubaydullayev.expensetracker.domain.model.GoalStatus
import com.ubaydullayev.expensetracker.domain.model.SavingGoal
import com.ubaydullayev.expensetracker.domain.model.Transaction
import com.ubaydullayev.expensetracker.domain.model.UpcomingBill

fun TransactionEntity.toDomainModel(): Transaction {

    return Transaction(
        id = id,
        title = title,
        category = category,
        amount = amount,
        isIncome = isIncome,
        timestamp = timestamp,
        isRecurring = isRecurring,
        hasAttachment = hasAttachment,
        note = note
    )
}

// Domain -> Entity (bazaga yozganda ishlatiladi)
fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        title = title,
        category = category,
        amount = amount,
        isIncome = isIncome,
        timestamp = timestamp,
        isRecurring = isRecurring,
        hasAttachment = hasAttachment,
        note = note
    )
}

fun UpcomingBillEntity.toDomainModel(): UpcomingBill {
    return UpcomingBill(
        id = id,
        title = title,
        category = category,
        frequency = frequency,
        price = price,
        dueText = dueText,
        isUrgent = isUrgent,
    )
}

fun UpcomingBill.toEntity(): UpcomingBillEntity {
    return UpcomingBillEntity(
        id = id,
        title = title,
        category = category,
        frequency = frequency,
        price = price,
        dueText = dueText,
        isUrgent = isUrgent
    )
}


fun SavingGoalEntity.toDomainModel(): SavingGoal {
    return SavingGoal(
        id = id,
        title = title,
        category = category,
        estimatedDate = estimatedDate,
        priority = priority,
        targetDate = targetDate,
        currentAmount = currentAmount,
        targetAmount = targetAmount,
        status = try {
            GoalStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            GoalStatus.ACTIVE
        }
    )
}

fun SavingGoal.toEntity(): SavingGoalEntity {
    return SavingGoalEntity(
        id = id,
        title = title,
        category = category,
        estimatedDate = estimatedDate,
        priority = priority,
        targetDate = targetDate,
        currentAmount = currentAmount,
        targetAmount = targetAmount,
        status = status.name
    )
}

fun BudgetEntity.toDomainModel(): Budget {
    return Budget(
        id = id,
        title = title,
        category = category,
        spentAmount = spentAmount,
        limitAmount = limitAmount
    )
}

fun Budget.toEntity(): BudgetEntity {
    return BudgetEntity(
        id = id,
        title = title,
        category = category,
        spentAmount = spentAmount,
        limitAmount = limitAmount
    )
}

fun ContributionEntity.toDomainModel(): Contribution {
    return Contribution(
        id = id,
        goalId = goalId,
        amount = amount,
        timestamp = timestamp
    )
}

fun Contribution.toEntity(): ContributionEntity {
    return ContributionEntity(
        id = id,
        goalId = goalId,
        amount = amount,
        timestamp = timestamp
    )
}