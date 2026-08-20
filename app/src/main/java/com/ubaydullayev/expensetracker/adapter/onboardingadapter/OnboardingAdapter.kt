package com.ubaydullayev.expensetracker.adapter.onboardingadapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.databinding.ItemOnboardingPageBinding

class OnboardingAdapter(private val pages: List<OnboardingPage>) :
    RecyclerView.Adapter<OnboardingAdapter.PageVH>() {

    inner class PageVH(val binding: ItemOnboardingPageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageVH {
        val binding = ItemOnboardingPageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PageVH(binding)
    }

    override fun onBindViewHolder(holder: PageVH, position: Int) {
        val page = pages[position]
        val ctx = holder.binding.root.context

        holder.binding.panelBg.setImageResource(page.panelBg)
        holder.binding.imgOnboarding.setImageResource(page.glyph)

        holder.binding.txtChip.apply {
            text = page.chip
            backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(ctx, page.chipBg))
            setTextColor(ContextCompat.getColor(ctx, page.chipText))
        }

        holder.binding.pill1Text.text = page.pill1
        holder.binding.pill1Dot.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(ctx, page.pill1Dot))
        holder.binding.pill2Text.text = page.pill2
        holder.binding.pill2Dot.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(ctx, page.pill2Dot))

        holder.binding.txtTitle.text = page.title
        holder.binding.txtSubtitle.text = page.subtitle
    }

    override fun getItemCount() = pages.size
}