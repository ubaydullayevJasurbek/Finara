package com.ubaydullayev.expensetracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.databinding.ItemOnboardingPageBinding

class OnboardingAdapter(private val pages: List<OnboardingPage>) :
    RecyclerView.Adapter<OnboardingAdapter.PageVH>() {

    inner class PageVH(val binding: ItemOnboardingPageBinding) :
        RecyclerView.ViewHolder(binding.root);

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageVH {
        val binding = ItemOnboardingPageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PageVH(binding)
    }

    override fun onBindViewHolder(holder: PageVH, position: Int) {
        val pages = pages[position]
        holder.binding.imgOnboarding.setImageResource(pages.imageRes)
        holder.binding.txtTitle.text = pages.title
        holder.binding.txtSubtitle.text = pages.subtitle
    }

    override fun getItemCount() = pages.size
}