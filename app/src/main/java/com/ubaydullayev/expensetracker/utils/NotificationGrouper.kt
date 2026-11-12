package com.ubaydullayev.expensetracker.utils

import com.ubaydullayev.expensetracker.domain.model.Notification
import com.ubaydullayev.expensetracker.presentation.notifications.NotificationListItem
import java.util.Calendar

object NotificationGrouper {

    /** Ro'yxat timestamp bo'yicha kamayish tartibida kelishi kerak (DAO shunday beradi). */
    fun group(
        notifications: List<Notification>,
        now: Long = System.currentTimeMillis()
    ): List<NotificationListItem> {
        val todayStart = startOfDay(now, daysAgo = 0)
        val yesterdayStart = startOfDay(now, daysAgo = 1)

        val today = notifications.filter { it.timestamp >= todayStart }
        val yesterday = notifications.filter { it.timestamp in yesterdayStart until todayStart }
        val earlier = notifications.filter { it.timestamp < yesterdayStart }

        return buildList {
            addSection("Today", today)
            addSection("Yesterday", yesterday)
            addSection("Earlier", earlier)
        }
    }

    private fun MutableList<NotificationListItem>.addSection(
        title: String,
        items: List<Notification>
    ) {
        if (items.isEmpty()) return
        add(NotificationListItem.Header(title, newCount = items.count { !it.isRead }))
        items.forEach { add(NotificationListItem.Item(it)) }
    }

    private fun startOfDay(now: Long, daysAgo: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -daysAgo)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}