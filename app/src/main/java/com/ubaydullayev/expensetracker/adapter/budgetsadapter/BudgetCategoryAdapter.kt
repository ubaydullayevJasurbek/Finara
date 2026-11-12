package com.ubaydullayev.expensetracker.adapter.budgetadapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.databinding.ItemBudgetCategoryBinding
import com.ubaydullayev.expensetracker.domain.model.Budget
import com.ubaydullayev.expensetracker.utils.CategoryStyleProvider
import androidx.core.graphics.toColorInt
import com.ubaydullayev.expensetracker.utils.formatAmount

class BudgetCategoryAdapter(
    private var items: List<Budget>,
    private val onDeleteClick: (Budget) -> Unit
) : RecyclerView.Adapter<BudgetCategoryAdapter.ViewHolder>() {

    private var isEditMode = false

    inner class ViewHolder(val binding: ItemBudgetCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBudgetCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val b = holder.binding
        val style = CategoryStyleProvider.getStyle(item.category)

        b.ivCategoryIcon.setImageResource(style.iconRes)
        b.tvCategoryName.text = item.title

        val percent = if (item.limitAmount > 0) {
            ((item.spentAmount / item.limitAmount) * 100).toInt()
        } else {
            0
        }

        b.progressCategory.progress = percent.coerceAtMost(100)
        b.tvSpent.text = formatAmount(item.spentAmount)
        b.tvLimit.text = "/ ${formatAmount(item.limitAmount)}"

        val leftAmount = item.limitAmount - item.spentAmount
        b.tvLeft.text = if (leftAmount >= 0) {
            "${formatAmount(leftAmount)} left"
        } else {
            "${formatAmount(-leftAmount)} over"
        }

        b.tvPercent.text = "$percent%"
        applyStatusColors(b, percent)

        b.tvForecast.text = "Spent ${formatAmount(item.spentAmount)} so far"
        b.ivTrend.setImageResource(
            if (percent < 80) R.drawable.ic_trend_down else R.drawable.ic_trend_up
        )

        // YANGI — Edit rejimi holatiga qarab ko'rsatish
        if (isEditMode) {
            b.cardStatus.visibility = android.view.View.GONE
            b.btnDeleteBudget.visibility = android.view.View.VISIBLE
            b.btnDeleteBudget.setOnClickListener {
                onDeleteClick(item)
            }
        } else {
            b.cardStatus.visibility = android.view.View.VISIBLE
            b.btnDeleteBudget.visibility = android.view.View.GONE
        }
    }

    private fun applyStatusColors(b: ItemBudgetCategoryBinding, percent: Int) {
        val (bgColor, textColor, statusLabel) = when {
            percent < 80 -> Triple("#DDF5E8".toColorInt(), "#0F7B55".toColorInt(), "Safe")
            percent < 100 -> Triple("#FEF3C7".toColorInt(), "#92600A".toColorInt(), "Near limit")
            else -> Triple("#FFE4E6".toColorInt(), "#E11D48".toColorInt(), "Over")
        }

        b.cardStatus.setCardBackgroundColor(bgColor)
        b.tvStatus.text = statusLabel
        b.tvStatus.setTextColor(textColor)
        b.cardPercent.setCardBackgroundColor(bgColor)
        b.tvPercent.setTextColor(textColor)
        b.progressCategory.setIndicatorColor(textColor)
        b.ivTrend.setColorFilter(textColor)
        b.cardIcon.setCardBackgroundColor(bgColor)
        b.ivCategoryIcon.setColorFilter(textColor)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<Budget>) {
        items = newItems
        notifyDataSetChanged()
    }

    // YANGI — Edit rejimini yoqish/o'chirish
    @SuppressLint("NotifyDataSetChanged")
    fun setEditMode(enabled: Boolean) {
        isEditMode = enabled
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}