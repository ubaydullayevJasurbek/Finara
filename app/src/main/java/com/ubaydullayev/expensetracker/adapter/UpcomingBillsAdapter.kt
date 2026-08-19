package com.ubaydullayev.expensetracker.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.databinding.ItemUpcomingBillsCardBinding

class UpcomingBillsAdapter(
    private val bills: List<UpcomingBill>,
) : RecyclerView.Adapter<UpcomingBillsAdapter.BillVH>() {

    inner class BillVH(val binding: ItemUpcomingBillsCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillVH {
        val binding = ItemUpcomingBillsCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BillVH(binding)
    }

    override fun onBindViewHolder(holder: BillVH, position: Int) {
        val bill = bills[position]
        with(holder.binding) {
            title.text = bill.title
            subtitle.text = "${bill.frequency} • ${bill.category}"
            price.text = bill.price

            iconImage.setImageResource(bill.iconRes)
            iconImage.imageTintList = ColorStateList.valueOf(bill.dueColor)
            iconBackground.setCardBackgroundColor(withAlpha(bill.dueColor, 0x22))

            accentBar.setBackgroundColor(bill.dueColor)

            dueBadge.text = bill.dueText
            dueBadge.setTextColor(bill.dueColor)
            dueBadge.backgroundTintList = ColorStateList.valueOf(withAlpha(bill.dueColor, 0x1F))
        }
    }

    override fun getItemCount() = bills.size

    /** Same hue as [color] but with the given [alpha] (0..255), for soft tinted backgrounds. */
    private fun withAlpha(color: Int, alpha: Int): Int =
        Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
}
