package com.ubaydullayev.expensetracker.domain.repository

import com.ubaydullayev.expensetracker.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getAllNotifications(): Flow<List<Notification>>
    fun getUnreadCount(): Flow<Int>
    suspend fun addNotification(notification: Notification)
    suspend fun markAsRead(id: Long)
    suspend fun markAllAsRead()
}