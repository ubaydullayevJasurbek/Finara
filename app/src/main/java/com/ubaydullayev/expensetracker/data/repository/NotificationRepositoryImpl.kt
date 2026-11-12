package com.ubaydullayev.expensetracker.data.repository

import com.ubaydullayev.expensetracker.data.local.dao.NotificationDao
import com.ubaydullayev.expensetracker.data.local.mapper.toDomainModel
import com.ubaydullayev.expensetracker.data.local.mapper.toEntity
import com.ubaydullayev.expensetracker.domain.model.Notification
import com.ubaydullayev.expensetracker.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepositoryImpl(
    private val dao: NotificationDao
) : NotificationRepository {

    override fun getAllNotifications(): Flow<List<Notification>> =
        dao.getAllNotifications().map { list -> list.map { it.toDomainModel() } }

    override fun getUnreadCount(): Flow<Int> = dao.getUnreadCount()

    override suspend fun addNotification(notification: Notification) =
        dao.insert(notification.toEntity())

    override suspend fun markAsRead(id: Long) = dao.markAsRead(id)

    override suspend fun markAllAsRead() = dao.markAllAsRead()
}