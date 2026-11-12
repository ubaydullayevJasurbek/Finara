package com.ubaydullayev.expensetracker.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ubaydullayev.expensetracker.domain.model.Notification
import com.ubaydullayev.expensetracker.domain.model.NotificationType
import com.ubaydullayev.expensetracker.domain.repository.NotificationRepository
import com.ubaydullayev.expensetracker.utils.NotificationGrouper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val selectedFilter = MutableStateFlow(NotificationFilter.ALL)

    val uiState: StateFlow<NotificationsUiState> = combine(
        repository.getAllNotifications(),
        selectedFilter
    ) { all, filter ->
        buildUiState(all, filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationsUiState())

    fun selectFilter(filter: NotificationFilter) {
        selectedFilter.value = filter
    }

    fun markAsRead(id: Long) {
        viewModelScope.launch { repository.markAsRead(id) }
    }

    fun markAllAsRead() {
        viewModelScope.launch { repository.markAllAsRead() }
    }

    private fun buildUiState(
        all: List<Notification>,
        filter: NotificationFilter
    ): NotificationsUiState {
        val filtered = when (filter) {
            NotificationFilter.ALL -> all
            NotificationFilter.UNREAD -> all.filter { !it.isRead }
            NotificationFilter.BUDGET -> all.filter { it.type == NotificationType.BUDGET }
            NotificationFilter.BILLS -> all.filter { it.type == NotificationType.BILL }
            NotificationFilter.GOALS -> all.filter { it.type == NotificationType.GOAL }
        }
        return NotificationsUiState(
            selectedFilter = filter,
            unreadCount = all.count { !it.isRead },   // filtrlanmagan ro'yxatdan
            items = NotificationGrouper.group(filtered)
        )
    }
}