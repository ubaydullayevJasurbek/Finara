package com.ubaydullayev.expensetracker.presentation.notifications

data class NotificationsUiState(
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
    val unreadCount: Int = 0,
    val items: List<NotificationListItem> = emptyList()
) {
    // Hisoblangan qiymat: saqlanmaydi, items'dan olinadi
    val isEmpty: Boolean get() = items.isEmpty()
}