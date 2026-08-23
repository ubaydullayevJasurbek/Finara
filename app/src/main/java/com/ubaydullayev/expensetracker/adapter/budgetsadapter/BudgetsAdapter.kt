package com.ubaydullayev.expensetracker.adapter.budgetsadapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.play.core.integrity.v
import com.ubaydullayev.expensetracker.databinding.ItemBudgetCardsBinding
import com.ubaydullayev.expensetracker.domain.model.Budget
import com.ubaydullayev.expensetracker.utils.CategoryStyleProvider
import androidx.core.graphics.toColorInt

class BudgetsAdapter(private var items: List<Budget>) :
    RecyclerView.Adapter<BudgetsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemBudgetCardsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding =
            ItemBudgetCardsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BudgetsAdapter.ViewHolder, position: Int) {
        val item = items[position]
        val b = holder.binding

        b.tvCategoryName.text = item.title
        b.tvSpent.text = "$${formatAmount(item.spentAmount)}"
        b.tvLimit.text = "/ $${formatAmount(item.limitAmount)}"

        val style = CategoryStyleProvider.getStyle(item.category)
        b.iconImage.setImageResource(style.iconRes)

        val iconBg = b.iconContainer.background.mutate() as GradientDrawable
        iconBg.setColor(style.iconBgColor)

        ImageViewCompat.setImageTintList(
            b.iconImage,
            android.content.res.ColorStateList.valueOf(style.textColor)
        )

        val percent = ((item.spentAmount / item.limitAmount) * 100).toInt()


        b.progressContainer.post {
            val containerWidth = b.progressContainer.width
            val fillParams = b.progressFill.layoutParams
            fillParams.width = (containerWidth * (percent / 100f)).toInt()
            b.progressFill.layoutParams = fillParams
        }


        if (percent >= 90) {
            b.tvCaption.text = "$percent% · Near limit"
            b.tvCaption.setTextColor("#E53935".toColorInt())
            val fillBg = b.progressFill.background.mutate() as GradientDrawable
            fillBg.setColor("#E53935".toColorInt())
        } else {
            b.tvCaption.text = "$percent% used"
            b.tvCaption.setTextColor("#6B7280".toColorInt())
            val fillBg = b.progressFill.background.mutate() as GradientDrawable
            fillBg.setColor("#0B7E57".toColorInt())
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<Budget>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    @SuppressLint("DefaultLocale")
    private fun formatAmount(amount: Double): String {
        return String.format("%,.0f", amount)
    }
}