package com.ubaydullayev.expensetracker.adapter.budgetsadapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.databinding.ItemBudgetCategoryBinding
import com.ubaydullayev.expensetracker.presentation.budget.BudgetCategory
import com.ubaydullayev.expensetracker.presentation.budget.BudgetStatus

class CategoryAdapter(private val items: List<BudgetCategory>) :
    RecyclerView.Adapter<CategoryAdapter.VH>() {

    inner class VH(val binding: ItemBudgetCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemBudgetCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int) = with(holder.binding) {
        val item = items[position]
        val s = item.status
        tvCategoryName.text = item.name
        tvForecast.text = item.forecastText
        tvSpent.text = "$${item.spent}"
        tvLimit.text = "/ $${item.limit}"
        tvLeft.text = item.leftText
        tvPercent.text = "${item.percent}%"
        tvStatus.text = s.label
        ivCategoryIcon.setImageResource(item.iconRes)
        ivCategoryIcon.setColorFilter(item.iconTint)
        cardIcon.setCardBackgroundColor(item.iconBgColor)
        progressCategory.setProgressCompat(item.percent.coerceAtMost(100), false)
        progressCategory.setIndicatorColor(s.main)
        ivTrend.setImageResource(if (item.isOverForecast) R.drawable.ic_trend_up else R.drawable.ic_trend_down)
        ivTrend.setColorFilter(if (item.isOverForecast) 0xFFD92D20.toInt() else 0xFF0F7B55.toInt())
        cardStatus.setCardBackgroundColor(s.soft); tvStatus.setTextColor(s.main)
        cardPercent.setCardBackgroundColor(s.soft); tvPercent.setTextColor(s.main)
        tvLeft.setTextColor(if (s == BudgetStatus.OVERSPENT) s.main else 0xFF6B7280.toInt())
    }
}