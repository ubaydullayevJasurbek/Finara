package com.ubaydullayev.expensetracker.presentation.addmoney

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.databinding.ItemSelectGoalBinding
import com.ubaydullayev.expensetracker.domain.model.SavingGoal

class GoalPickerAdapter(
    private val goals: List<SavingGoal>,
    private val onGoalClick: (SavingGoal) -> Unit
) : RecyclerView.Adapter<GoalPickerAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSelectGoalBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelectGoalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val goal = goals[position]
        val b = holder.binding

        b.tvSelectGoalName.text = goal.title
        val percent = ((goal.currentAmount / goal.targetAmount) * 100).toInt()
        b.tvSelectGoalProgress.text = "$percent%"

        b.root.setOnClickListener {
            onGoalClick(goal)
        }
    }

    override fun getItemCount() = goals.size
}