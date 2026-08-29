package com.ubaydullayev.expensetracker.adapter.transactionadapter

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.databinding.ItemDayHeaderBinding
import com.ubaydullayev.expensetracker.databinding.ItemTransactionBinding
import com.ubaydullayev.expensetracker.presentation.transaction.TransactionListItem
import com.ubaydullayev.expensetracker.utils.CategoryStyleProvider
import com.ubaydullayev.expensetracker.utils.DateFormatter

class GroupedTransactionsAdapter(
    private var items: List<TransactionListItem>,
    private val onTransactionClick: (Long) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_TRANSACTION = 1
    }

    // ===== ViewHolder'lar =====
    inner class HeaderViewHolder(val binding: ItemDayHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class TransactionViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    // ===== Qaysi turdagi qator ekanini aniqlash =====
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is TransactionListItem.DateHeader -> TYPE_HEADER
            is TransactionListItem.TransactionRow -> TYPE_TRANSACTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val binding = ItemDayHeaderBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            HeaderViewHolder(binding)
        } else {
            val binding = ItemTransactionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            TransactionViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is TransactionListItem.DateHeader -> bindHeader(holder as HeaderViewHolder, item)
            is TransactionListItem.TransactionRow -> bindTransaction(
                holder as TransactionViewHolder,
                item,
                position
            )
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun bindHeader(holder: HeaderViewHolder, item: TransactionListItem.DateHeader) {
        val b = holder.binding
        b.dayLabel.text = item.label

        val sign = if (item.totalAmount >= 0) "+" else "−"
        b.dayTotal.text = "$sign$${String.format("%,.2f", kotlin.math.abs(item.totalAmount))}"
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun bindTransaction(
        holder: TransactionViewHolder,
        item: TransactionListItem.TransactionRow,
        position: Int
    ) {
        val transaction = item.transaction
        val b = holder.binding

        b.title.text = transaction.title
        b.dateTime.text = DateFormatter.formatTransactionDate(transaction.timestamp)

        val style = CategoryStyleProvider.getStyle(transaction.category)
        b.icon.setImageResource(style.iconRes)
        b.iconBackground.setCardBackgroundColor(style.iconBgColor)

        b.categoryBadge.text = transaction.category
        b.categoryBadge.setTextColor(style.textColor)
        val badgeBg = b.categoryBadge.background.mutate() as GradientDrawable
        badgeBg.setColor(style.iconBgColor)

        val sign = if (transaction.isIncome) "+" else "−"
        b.amount.text = "$sign$${String.format("%,.2f", transaction.amount)}"
        b.amount.setTextColor(
            if (transaction.isIncome) "#2E7D32".toColorInt() else "#1A1A1A".toColorInt()
        )

        when {
            transaction.isRecurring -> {
                b.statusIcon.visibility = View.VISIBLE
                b.statusIcon.setImageResource(R.drawable.ic_refresh)
            }

            transaction.hasAttachment -> {
                b.statusIcon.visibility = View.VISIBLE
                b.statusIcon.setImageResource(R.drawable.ic_attachment)
            }

            else -> {
                b.statusIcon.visibility = View.GONE
            }
        }

        b.root.setOnClickListener {
            onTransactionClick(transaction.id)
        }
        // Divider: keyingi element ham TransactionRow bo'lsagina ko'rsatamiz
        // (agar keyingisi DateHeader yoki oxiri bo'lsa, divider yashiriladi)
        val isLastInGroup =
            position == items.size - 1 || items[position + 1] is TransactionListItem.DateHeader
        b.divider.visibility = if (isLastInGroup) View.GONE else View.VISIBLE
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<TransactionListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}