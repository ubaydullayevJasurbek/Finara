package com.ubaydullayev.expensetracker.adapter.savinggoaladapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.data.local.entity.SavingGoalEntity
import com.ubaydullayev.expensetracker.databinding.ItemGoalCardBinding
import com.ubaydullayev.expensetracker.domain.model.SavingGoal
import com.ubaydullayev.expensetracker.utils.CategoryStyleProvider

class SavingGoalDataAdapter(private var items: List<SavingGoal>) :
    RecyclerView.Adapter<SavingGoalDataAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemGoalCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ItemGoalCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val b = holder.binding

        b.tvGoalName.text = item.title
        b.tvCategoryTag.text = item.category
        b.tvEstDate.text = item.estimatedDate
        b.tvPriority.text = item.priority
        b.tvTargetDate.text = item.targetDate

        val style = CategoryStyleProvider.getStyle(item.category)
        b.ivGoalIcon.setImageResource(style.iconRes)

        b.tvSaved.text = "$${formatAmount(item.currentAmount)}"
        b.tvTargetAmount.text = "/ $${formatAmount(item.targetAmount)}"

        val percent = ((item.currentAmount / item.targetAmount) * 100).toInt()
        b.tvPercent.text = "$percent%"
        b.goalProgressRing.progress = percent

        val remaining = item.targetAmount - item.currentAmount
        b.tvToGo.text = "$${formatAmount(remaining)} to go"

        val fillParams = b.goalProgressFill.layoutParams as ConstraintLayout.LayoutParams
        fillParams.matchConstraintPercentWidth = percent / 100f
        b.goalProgressFill.layoutParams = fillParams
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<SavingGoal>) {
        items = newItems
        notifyDataSetChanged()
    }

    @SuppressLint("DefaultLocale")
    private fun formatAmount(amount: Double): String {
        return String.format("%,.0f", amount)
    }

    override fun getItemCount() = items.size

}