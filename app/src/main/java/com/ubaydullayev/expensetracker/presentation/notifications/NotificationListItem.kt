package com.ubaydullayev.expensetracker.presentation.notifications

import com.ubaydullayev.expensetracker.domain.model.Notification

sealed class NotificationListItem {
    /** "Today" / "Yesterday" / "Earlier" sarlavhasi, newCount = shu guruhdagi o'qilmaganlar */
    data class Header(val title: String, val newCount: Int) : NotificationListItem()

    data class Item(val notification: Notification) : NotificationListItem()
}