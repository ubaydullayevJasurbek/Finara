package com.ubaydullayev.expensetracker.adapter.transactionadapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.databinding.ItemTransactionBinding

class TransactionsAdapter(
    private val items: List<Transaction>
) : RecyclerView.Adapter<TransactionsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val b = holder.binding

        b.title.text = item.title
        b.dateTime.text = item.dateTime
        b.icon.setImageResource(item.iconRes)
        b.iconBackground.setCardBackgroundColor(item.iconBgColor)

        b.categoryBadge.text = item.category
        b.categoryBadge.setTextColor(item.categoryColor)
        val badgeBg = b.categoryBadge.background.mutate() as GradientDrawable
        badgeBg.setColor(item.categoryBgColor)

        val sign = if (item.isIncome) "+" else "−"
        b.amount.text = "$sign$${String.format("%,.2f", item.amount)}"
        b.amount.setTextColor(
            if (item.isIncome) Color.parseColor("#2E7D32") else Color.parseColor("#1A1A1A")
        )

        // Oxirgi elementda divider yashirin
        b.divider.visibility = if (position == items.size - 1) View.GONE else View.VISIBLE
    }

    override fun getItemCount() = items.size
}