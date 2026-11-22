package com.ubaydullayev.expensetracker.adapter.budgetadapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.databinding.ItemBudgetAddTileBinding
import com.ubaydullayev.expensetracker.databinding.ItemBudgetTileBinding
import com.ubaydullayev.expensetracker.databinding.ItemBudgetTipTileBinding
import com.ubaydullayev.expensetracker.domain.model.Budget
import com.ubaydullayev.expensetracker.utils.CategoryStyleProvider
import com.ubaydullayev.expensetracker.utils.formatAmount

/**
 * Budget ekranining 2 ustunli to'ri (Mozaika dizayni):
 * [budjet plitkalari...] + "New budget" plitkasi + "Smart tip" plitkasi.
 */
class BudgetTileAdapter(
    private val onDeleteClick: (Budget) -> Unit,
    private val onAddClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var budgets: List<Budget> = emptyList()
    private var tipText: String = ""
    private var isEditMode = false

    private companion object {
        const val TYPE_BUDGET = 0
        const val TYPE_ADD = 1
        const val TYPE_TIP = 2
    }

    class BudgetHolder(val binding: ItemBudgetTileBinding) : RecyclerView.ViewHolder(binding.root)
    class AddHolder(val binding: ItemBudgetAddTileBinding) : RecyclerView.ViewHolder(binding.root)
    class TipHolder(val binding: ItemBudgetTipTileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount() = budgets.size + 1 + (if (tipText.isNotBlank()) 1 else 0)

    override fun getItemViewType(position: Int) = when {
        position < budgets.size -> TYPE_BUDGET
        position == budgets.size -> TYPE_ADD
        else -> TYPE_TIP
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_BUDGET -> BudgetHolder(ItemBudgetTileBinding.inflate(inflater, parent, false))
            TYPE_ADD -> AddHolder(ItemBudgetAddTileBinding.inflate(inflater, parent, false))
            else -> TipHolder(ItemBudgetTipTileBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BudgetHolder -> bindBudget(holder.binding, budgets[position])
            is AddHolder -> holder.binding.addTile.setOnClickListener { onAddClick() }
            is TipHolder -> holder.binding.tvSmartTip.text = tipText
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindBudget(b: ItemBudgetTileBinding, item: Budget) {
        val context = b.root.context
        val percent =
            if (item.limitAmount > 0) ((item.spentAmount / item.limitAmount) * 100).toInt() else 0

        b.ivCategoryIcon.setImageResource(CategoryStyleProvider.getStyle(item.category).iconRes)
        b.tvCategoryName.text = item.title
        b.tvAmounts.text = "${formatAmount(item.spentAmount)} / ${formatAmount(item.limitAmount)}"
        b.tvPercent.text = "$percent%"
        b.progressRing.progress = percent.coerceAtMost(100)

        // Holat: 80% gacha Safe (yashil), 80-99% Near limit (sariq), 100%+ Over (qizil)
        val (mainRes, softRes, labelRes) = when {
            percent < 80 -> Triple(R.color.fin_success, R.color.fin_success_tint, "Safe")
            percent < 100 -> Triple(R.color.fin_warning, R.color.fin_warning_tint, "Near limit")
            else -> Triple(R.color.fin_error, R.color.fin_error_tint, "Over")
        }
        val main = ContextCompat.getColor(context, mainRes)
        val soft = ContextCompat.getColor(context, softRes)
        b.progressRing.setIndicatorColor(main)
        b.cardIcon.setCardBackgroundColor(soft)
        b.ivCategoryIcon.imageTintList = ColorStateList.valueOf(main)
        b.tvStatus.text = labelRes
        b.tvStatus.setTextColor(main)
        b.tvStatus.backgroundTintList = ColorStateList.valueOf(soft)

        // Edit rejimi: halqa o'rnida o'chirish tugmasi
        b.ringContainer.visibility = if (isEditMode) View.GONE else View.VISIBLE
        b.btnDeleteBudget.visibility = if (isEditMode) View.VISIBLE else View.GONE
        b.btnDeleteBudget.setOnClickListener { onDeleteClick(item) }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newBudgets: List<Budget>, newTipText: String) {
        budgets = newBudgets
        tipText = newTipText
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setEditMode(enabled: Boolean) {
        isEditMode = enabled
        notifyDataSetChanged()
    }
}