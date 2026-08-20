package com.ubaydullayev.expensetracker.adapter.upcomingbillsadapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.databinding.ItemUpcomingBillsCardBinding

class UpcomingBillsAdapter(
    private val items: List<UpcomingBill>
) : RecyclerView.Adapter<UpcomingBillsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemUpcomingBillsCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUpcomingBillsCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val b = holder.binding
        val context = b.root.context

        b.title.text = item.title
        b.subtitle.text = "${item.frequency} · ${item.category}"
        b.price.text = "$${String.format("%,.2f", item.price)}"
        b.dueBadge.text = item.dueText
        b.iconImage.setImageResource(item.iconRes)
        b.iconBackground.setCardBackgroundColor(item.iconRes)

        // isUrgent ga qarab rang avtomatik tanlanadi
        val (badgeBg, badgeText, accentColor) = if (item.isUrgent) {
            Triple(
                ContextCompat.getColor(context, R.color.red_bg),
                ContextCompat.getColor(context, R.color.red_text),
                ContextCompat.getColor(context, R.color.red_text)
            )
        } else {
            Triple(
                ContextCompat.getColor(context, R.color.green_bg),
                ContextCompat.getColor(context, R.color.green_text),
                ContextCompat.getColor(context, R.color.green_text)
            )
        }

        val bg = b.dueBadge.background.mutate() as GradientDrawable
        bg.setColor(badgeBg)
        b.dueBadge.setTextColor(badgeText)
        b.accentBar.setBackgroundColor(accentColor)
    }

    override fun getItemCount() = items.size
}