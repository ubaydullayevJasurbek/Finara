package com.ubaydullayev.expensetracker.adapter.savinggoaladapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.databinding.ItemActiveSavingGoalBinding
import com.ubaydullayev.expensetracker.domain.model.SavingGoal
import com.ubaydullayev.expensetracker.utils.GoalColorProvider

class ActiveGoalsAdapter(
    private var items: List<SavingGoal>
) : RecyclerView.Adapter<ActiveGoalsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemActiveSavingGoalBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActiveSavingGoalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val b = holder.binding
        val scheme = GoalColorProvider.getScheme(item.category)

        b.bannerContainer.setBackgroundResource(scheme.bannerBg)
        b.ivGoalIcon.setImageResource(scheme.iconRes)
        b.tvPriority.text = item.priority
        b.tvTargetDate.text = item.targetDate

        b.tvGoalName.text = item.title
        b.tvCategoryTag.text = item.category
        b.tvCategoryTag.setTextColor(scheme.tagTextColor)
        b.tvEstDate.text = item.estimatedDate

        val percent = ((item.currentAmount / item.targetAmount) * 100).toInt()
        b.tvPercent.text = "$percent%"
        b.goalProgressRing.progress = percent
        b.goalProgressRing.setIndicatorColor(scheme.progressColor)

        b.tvSaved.text = "$${formatAmount(item.currentAmount)}"
        b.tvTargetAmount.text = "/ $${formatAmount(item.targetAmount)}"

        val remaining = item.targetAmount - item.currentAmount
        b.tvToGo.text = "$${formatAmount(remaining)} to go"

        val fillParams = b.goalProgressFill.layoutParams as ConstraintLayout.LayoutParams
        fillParams.matchConstraintPercentWidth = percent / 100f
        b.goalProgressFill.layoutParams = fillParams
        b.goalProgressFill.setBackgroundColor(scheme.progressColor)
    }

    @SuppressLint("DefaultLocale")
    private fun formatAmount(amount: Double) = String.format("%,.0f", amount)

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<SavingGoal>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}