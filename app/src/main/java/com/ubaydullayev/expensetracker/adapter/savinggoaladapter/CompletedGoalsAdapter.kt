package com.ubaydullayev.expensetracker.adapter.savinggoaladapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.databinding.ItemCompletedSavingGoalBinding
import com.ubaydullayev.expensetracker.domain.model.SavingGoal
import java.text.SimpleDateFormat
import java.util.Locale

class CompletedGoalsAdapter(
    private var items: List<SavingGoal>
) : RecyclerView.Adapter<CompletedGoalsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCompletedSavingGoalBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCompletedSavingGoalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val b = holder.binding

        b.tvCompletedName.text = item.title
        b.tvCompletedDate.text = "Target: ${item.targetDate}"
        b.tvCompletedAmount.text = "$${String.format("%,.0f", item.targetAmount)}"
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<SavingGoal>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}