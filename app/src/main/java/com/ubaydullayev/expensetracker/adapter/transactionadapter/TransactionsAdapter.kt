package com.ubaydullayev.expensetracker.adapter.transactionadapter

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.domain.model.Transaction
import com.ubaydullayev.expensetracker.databinding.ItemTransactionBinding
import com.ubaydullayev.expensetracker.utils.CategoryStyleProvider
import androidx.core.graphics.toColorInt
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.utils.DateFormatter

class TransactionsAdapter(
    private var items: List<Transaction>
) : RecyclerView.Adapter<TransactionsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val b = holder.binding

        b.title.text = item.title
        b.dateTime.text = DateFormatter.formatTransactionDate(item.timestamp)

        val style = CategoryStyleProvider.getStyle(item.category)
        b.icon.setImageResource(style.iconRes)
        b.iconBackground.setCardBackgroundColor(style.iconBgColor)

        b.categoryBadge.text = item.category
        b.categoryBadge.setTextColor(style.textColor)
        val badgeBg = b.categoryBadge.background.mutate() as GradientDrawable
        badgeBg.setColor(style.iconBgColor)

        val sign = if (item.isIncome) "+" else "−"
        b.amount.text = "$sign$${String.format("%,.2f", item.amount)}"
        b.amount.setTextColor(
            if (item.isIncome) "#2E7D32".toColorInt() else "#1A1A1A".toColorInt()
        )

        // YANGI — status icon (recurring yoki attachment, ikkalasi bir vaqtda bo'lmaydi)
        when {
            item.isRecurring -> {
                b.statusIcon.visibility = View.VISIBLE
                b.statusIcon.setImageResource(R.drawable.ic_refresh)
            }
            item.hasAttachment -> {
                b.statusIcon.visibility = View.VISIBLE
                b.statusIcon.setImageResource(R.drawable.ic_attachment)
            }
            else -> {
                b.statusIcon.visibility = View.GONE
            }
        }

        b.divider.visibility = if (position == items.size - 1) View.GONE else View.VISIBLE
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<Transaction>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}