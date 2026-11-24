package com.ubaydullayev.expensetracker.adapter.recurringadapter

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.databinding.ItemRecurringPaymentBinding
import com.ubaydullayev.expensetracker.domain.model.RecurringPayment
import com.ubaydullayev.expensetracker.utils.CategoryStyleProvider
import com.ubaydullayev.expensetracker.utils.formatAmount
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Home'dagi "Upcoming payments": Recurring belgili xarajatlardan hisoblangan to'lovlar. */
class RecurringPaymentsAdapter(
    private var items: List<RecurringPayment> = emptyList()
) : RecyclerView.Adapter<RecurringPaymentsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRecurringPaymentBinding) : RecyclerView.ViewHolder(binding.root)

    private val dateFormat = SimpleDateFormat("MMM d", Locale.US)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemRecurringPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val b = holder.binding
        val ctx = b.root.context
        val res = ctx.resources

        b.tvTitle.text = item.title
        b.tvSubtitle.text = ctx.getString(R.string.recurring_subtitle, dateFormat.format(Date(item.nextDue)))
        b.tvAmount.text = formatAmount(item.amount)

        val style = CategoryStyleProvider.getStyle(item.category)
        b.iconImage.setImageResource(style.iconRes)
        b.iconImage.imageTintList = ColorStateList.valueOf(style.textColor)
        b.iconTile.background = GradientDrawable().apply {
            cornerRadius = 13f * res.displayMetrics.density
            setColor(style.iconBgColor)
        }

        val days = item.daysUntil
        b.tvDue.text = when {
            days < 0 -> res.getQuantityString(R.plurals.recurring_overdue_days, -days, -days)
            days == 0 -> ctx.getString(R.string.recurring_due_today)
            days == 1 -> ctx.getString(R.string.recurring_due_tomorrow)
            else -> res.getQuantityString(R.plurals.recurring_due_in_days, days, days)
        }

        // Yaqin yoki muddati o'tgan - qizil, qolganlari ko'k
        val (pillColor, textColor) = if (item.isUrgent) {
            R.color.fin_error_tint to R.color.fin_error
        } else {
            R.color.fin_blue_tint to R.color.fin_blue
        }
        b.tvDue.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(ctx, pillColor))
        b.tvDue.setTextColor(ContextCompat.getColor(ctx, textColor))
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<RecurringPayment>) {
        items = newItems
        notifyDataSetChanged()
    }
}
