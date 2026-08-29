package com.ubaydullayev.expensetracker.adapter.listadapter

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.databinding.ItemCategoryPickerBinding
import com.ubaydullayev.expensetracker.utils.CategoryStyleProvider

class CategoryPickerAdapter(
    private var categories: List<String>,
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryPickerAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: ItemCategoryPickerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding =
            ItemCategoryPickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        val b = holder.binding
        val style = CategoryStyleProvider.getStyle(category)

        b.categoryItemName.text = category
        b.categoryItemIcon.setImageResource(style.iconRes)

        // Icon foni — to'q rangli doira
        val iconBg = GradientDrawable()
        iconBg.shape = GradientDrawable.OVAL
        iconBg.setColor(style.textColor)
        b.categoryItemIconBg.background = iconBg

        // Kartaning O'ZI ham och rangli fon oladi (M3 "tonal" uslubi)
        (b.root)
            .setCardBackgroundColor(style.iconBgColor)

        b.root.setOnClickListener {
            onCategoryClick(category)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateCategories(newCategories: List<String>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun getItemCount() = categories.size
}