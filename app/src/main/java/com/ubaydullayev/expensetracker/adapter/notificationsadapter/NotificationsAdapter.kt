package com.ubaydullayev.expensetracker.presentation.notifications

import android.content.res.ColorStateList
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.databinding.ItemNotificationBinding
import com.ubaydullayev.expensetracker.databinding.ItemNotificationHeaderBinding
import com.ubaydullayev.expensetracker.domain.model.Notification
import com.ubaydullayev.expensetracker.domain.model.NotificationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt

class NotificationsAdapter(
    private val onItemClick: (Notification) -> Unit
) : ListAdapter<NotificationListItem, RecyclerView.ViewHolder>(DiffCallback) {

    // ---------- Tur -> ko'rinish xaritasi (bir joyda) ----------
    private data class TypeStyle(
        @DrawableRes val icon: Int,
        @StringRes val label: Int,
        @ColorInt val accent: Int,
        @ColorInt val softBg: Int
    )

    private fun styleOf(type: NotificationType): TypeStyle = when (type) {
        NotificationType.BUDGET -> TypeStyle(
            R.drawable.ic_pie_chart, R.string.filter_budget,
            "#D97706".toColorInt(), "#FEF3C7".toColorInt()
        )

        NotificationType.BILL -> TypeStyle(
            R.drawable.ic_event, R.string.filter_bills,
            "#2563EB".toColorInt(), "#DBEAFE".toColorInt()
        )

        NotificationType.GOAL -> TypeStyle(
            R.drawable.ic_target_1, R.string.filter_goals,
            "#0F7B55".toColorInt(), "#DDF5E8".toColorInt()
        )

        NotificationType.INCOME -> TypeStyle(
            R.drawable.ic_trending_up, R.string.type_income,
            "#0F7B55".toColorInt(), "#DDF5E8".toColorInt()
        )
    }

    // ---------- Ikki view type ----------
    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is NotificationListItem.Header -> TYPE_HEADER
        is NotificationListItem.Item -> TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(
                ItemNotificationHeaderBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            else -> ItemViewHolder(ItemNotificationBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is NotificationListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is NotificationListItem.Item -> (holder as ItemViewHolder).bind(item.notification)
        }
    }

    // ---------- Header ----------
    inner class HeaderViewHolder(
        private val binding: ItemNotificationHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationListItem.Header) {
            binding.tvSectionTitle.text = item.title
            if (item.newCount > 0) {
                binding.tvSectionCount.visibility = View.VISIBLE
                binding.tvSectionCount.text = binding.root.context
                    .getString(R.string.section_new_count, item.newCount)
            } else {
                binding.tvSectionCount.visibility = View.GONE
            }
        }
    }

    // ---------- Item ----------
    inner class ItemViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(n: Notification) {
            val ctx = binding.root.context
            val style = styleOf(n.type)
            val density = ctx.resources.displayMetrics.density

            binding.tvTitle.text = n.title
            binding.tvMessage.text = n.message
            binding.tvTime.text = formatTime(n.timestamp)

            // Tur: ikonka, fon, yorliq
            binding.ivTypeIcon.setImageResource(style.icon)
            binding.ivTypeIcon.imageTintList = ColorStateList.valueOf(style.accent)
            binding.cardIconBg.setCardBackgroundColor(style.softBg)
            binding.cardType.setCardBackgroundColor(style.softBg)
            binding.tvType.setText(style.label)
            binding.tvType.setTextColor(style.accent)

            // Holat: o'qilmagan -> rangli ramka + nuqta, o'qilgan -> kulrang, nuqtasiz
            if (n.isRead) {
                binding.cardNotification.strokeColor = "#E8EBEF".toColorInt()
                binding.cardNotification.strokeWidth = (1 * density).toInt()
                binding.viewUnreadDot.visibility = View.GONE
            } else {
                binding.cardNotification.strokeColor = style.accent
                binding.cardNotification.strokeWidth = (1.5f * density).toInt()
                binding.viewUnreadDot.visibility = View.VISIBLE
                binding.viewUnreadDot.backgroundTintList = ColorStateList.valueOf(style.accent)
            }

            binding.cardNotification.setOnClickListener { onItemClick(n) }
        }
    }

    // ---------- Vaqt matni ----------
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    private fun formatTime(millis: Long): String {
        val isToday = DateUtils.isToday(millis)
        val isYesterday = DateUtils.isToday(millis + DateUtils.DAY_IN_MILLIS)
        return if (isToday || isYesterday) {
            timeFormat.format(Date(millis))
        } else {
            dateFormat.format(Date(millis))
        }
    }

    // ---------- DiffUtil ----------
    private object DiffCallback : DiffUtil.ItemCallback<NotificationListItem>() {
        override fun areItemsTheSame(
            old: NotificationListItem,
            new: NotificationListItem
        ): Boolean =
            when {
                old is NotificationListItem.Header && new is NotificationListItem.Header ->
                    old.title == new.title

                old is NotificationListItem.Item && new is NotificationListItem.Item ->
                    old.notification.id == new.notification.id

                else -> false
            }

        override fun areContentsTheSame(
            old: NotificationListItem,
            new: NotificationListItem
        ): Boolean =
            old == new
    }

    private companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1
    }
}