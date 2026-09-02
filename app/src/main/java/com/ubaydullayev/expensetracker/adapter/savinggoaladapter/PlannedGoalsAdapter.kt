package com.ubaydullayev.expensetracker.adapter.savinggoaladapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.databinding.ItemPlannedSavingGoalBinding
import com.ubaydullayev.expensetracker.domain.model.SavingGoal

class PlannedGoalsAdapter(
    private var items: List<SavingGoal>
) : RecyclerView.Adapter<PlannedGoalsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPlannedSavingGoalBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlannedSavingGoalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val b = holder.binding

        b.tvPlannedName.text = item.title
        b.tvPlannedTarget.text = "Target: $${String.format("%,.0f", item.targetAmount)}"
    }

    fun updateData(newItems: List<SavingGoal>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}