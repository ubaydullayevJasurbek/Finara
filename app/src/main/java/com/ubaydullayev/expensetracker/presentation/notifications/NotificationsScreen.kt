package com.ubaydullayev.expensetracker.presentation.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.data.local.database.AppDatabase
import com.ubaydullayev.expensetracker.data.repository.NotificationRepositoryImpl
import com.ubaydullayev.expensetracker.databinding.ScreenNotificationsBinding
import com.ubaydullayev.expensetracker.domain.model.NotificationType
import com.ubaydullayev.expensetracker.presentation.common.BaseViewModelFragment
import kotlinx.coroutines.launch

class NotificationsScreen :
    BaseViewModelFragment<ScreenNotificationsBinding, NotificationsViewModel>(
        NotificationsViewModel::class.java
    ) {

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ScreenNotificationsBinding = ScreenNotificationsBinding.inflate(inflater, container, false)

    override val viewModelFactory: ViewModelProvider.Factory
        get() = NotificationsViewModelFactory(
            NotificationRepositoryImpl(
                AppDatabase.getInstance(requireContext()).notificationDao()
            )
        )

    private val adapter = NotificationsAdapter { notification ->
        if (!notification.isRead) viewModel.markAsRead(notification.id)
        openTarget(notification.type)
    }

    private fun openTarget(type: NotificationType) {
        val destination = when (type) {
            NotificationType.BUDGET -> R.id.budgetScreen
            NotificationType.GOAL -> R.id.savingGoalsScreen
            NotificationType.BILL, NotificationType.INCOME -> null
        }
        destination?.let { findNavController().navigate(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupList()
        setupFilters()
        setupActions()
        observeState()
    }


    private fun setupList() {
        binding.rvNotifications.adapter = adapter
    }

    private fun setupFilters() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chipUnread -> NotificationFilter.UNREAD
                R.id.chipBudget -> NotificationFilter.BUDGET
                R.id.chipBills -> NotificationFilter.BILLS
                R.id.chipGoals -> NotificationFilter.GOALS
                else -> NotificationFilter.ALL
            }
            viewModel.selectFilter(filter)
        }
    }


    private fun setupActions() {
        binding.btnMarkAllRead.setOnClickListener { viewModel.markAllAsRead() }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    render(state)
                }
            }
        }
    }

    private fun render(state: NotificationsUiState) {
        // Ro'yxat yoki bo'sh holat
        adapter.submitList(state.items)
        binding.rvNotifications.isVisible = !state.isEmpty
        binding.emptyState.isVisible = state.isEmpty

        // Sarlavha: nishon va tagma'no
        val hasUnread = state.unreadCount > 0
        binding.cardUnreadBadge.isVisible = hasUnread
        binding.tvUnreadBadge.text = state.unreadCount.toString()
        binding.tvHeaderSubtitle.text = if (hasUnread) {
            getString(R.string.notifications_unread_subtitle, state.unreadCount)
        } else {
            getString(R.string.notifications_all_read)
        }
        binding.btnMarkAllRead.isVisible = hasUnread

        // "Unread" chipida son
        binding.chipUnread.text = if (hasUnread) {
            getString(R.string.filter_unread_count, state.unreadCount)
        } else {
            getString(R.string.filter_unread)
        }
    }
}