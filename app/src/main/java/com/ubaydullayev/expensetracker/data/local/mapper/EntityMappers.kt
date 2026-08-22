package com.ubaydullayev.expensetracker.data.local.mapper

import com.ubaydullayev.expensetracker.data.local.entity.SavingGoalEntity
import com.ubaydullayev.expensetracker.data.local.entity.TransactionEntity
import com.ubaydullayev.expensetracker.data.local.entity.UpcomingBillEntity
import com.ubaydullayev.expensetracker.domain.model.SavingGoal
import com.ubaydullayev.expensetracker.domain.model.Transaction
import com.ubaydullayev.expensetracker.domain.model.UpcomingBill

fun TransactionEntity.toDomainModel(): Transaction{

    return Transaction(
        id = id,
        title = title,
        category = category,
        dateTime = dateTime,
        amount = amount,
        isIncome = isIncome
    )
}

// Domain -> Entity (bazaga yozganda ishlatiladi)
fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        title = title,
        category = category,
        dateTime = dateTime,
        amount = amount,
        isIncome = isIncome
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
        isUrgent = isUrgent
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
        targetAmount = targetAmount
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
        targetAmount = targetAmount
    )
}