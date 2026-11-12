package com.ubaydullayev.expensetracker.domain.model

data class Notification(

    val id: Long = 0,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long,
    val isRead: Boolean = false
)