package com.ubaydullayev.expensetracker.presentation.budget

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Rect
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.adapter.budgetadapter.BudgetTileAdapter
import com.ubaydullayev.expensetracker.data.local.database.AppDatabase
import com.ubaydullayev.expensetracker.data.repository.BudgetRepositoryImpl
import com.ubaydullayev.expensetracker.databinding.DialogAddBudgetBinding
import com.ubaydullayev.expensetracker.databinding.ScreenBudgetBinding
import com.ubaydullayev.expensetracker.presentation.addTransaction.categorysheet.CategoryPickerSheet
import com.ubaydullayev.expensetracker.presentation.common.BaseViewModelFragment
import kotlinx.coroutines.launch
import com.ubaydullayev.expensetracker.domain.model.Budget
import com.ubaydullayev.expensetracker.utils.CurrencyFormatter
import com.ubaydullayev.expensetracker.utils.formatAmount

class BudgetScreen :
    BaseViewModelFragment<ScreenBudgetBinding, BudgetsViewModel>(BudgetsViewModel::class.java) {

    override val viewModelFactory: ViewModelProvider.Factory
        get() {
            val database = AppDatabase.getInstance(requireContext())
            return BudgetsViewModelFactory(
                budgetRepository = BudgetRepositoryImpl(database.budgetDao())
            )
        }

    private lateinit var categoryAdapter: BudgetTileAdapter

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenBudgetBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEditButton()
        setupRecyclerView()
        observeData()
    }

    private fun showAddBudgetDialog() {
        val dialogBinding = DialogAddBudgetBinding.inflate(layoutInflater)
        var selectedCategory: String? = null

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.RoundedMaterialDialog)
            .setView(dialogBinding.root)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialogBinding.rowSelectBudgetCategory.setOnClickListener {
            CategoryPickerSheet().show(childFragmentManager, "CategoryPickerSheet")

        }
        childFragmentManager.setFragmentResultListener(
            CategoryPickerSheet.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val category = bundle.getString(CategoryPickerSheet.RESULT_CATEGORY)
                ?: return@setFragmentResultListener
            selectedCategory = category
            dialogBinding.tvSelectedBudgetCategory.text = category
            dialogBinding.tvSelectedBudgetCategory.setTextColor(color(R.color.fin_navy))
        }

        dialog.setOnShowListener {
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.setTextColor(color(R.color.fin_blue))

            val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            cancelButton.setTextColor(color(R.color.fin_text_secondary))

            addButton.setOnClickListener {
                val category = selectedCategory
                val limitText = dialogBinding.etBudgetLimit.text.toString()
                val limit = limitText.toDoubleOrNull()

                when {
                    category == null -> {
                        shakeView(dialogBinding.rowSelectBudgetCategory)
                    }

                    viewModel.hasBudgetFor(category) -> {
                        dialogBinding.tvSelectedBudgetCategory.setTextColor(color(R.color.fin_error))
                        shakeView(dialogBinding.rowSelectBudgetCategory)
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.budget_already_exists, category),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    limit == null || limit <= 0.0 -> {
                        dialogBinding.tilBudgetLimit.error = "Enter a valid amount"
                        shakeView(dialogBinding.tilBudgetLimit)
                    }

                    else -> {
                        viewModel.addBudget(category, CurrencyFormatter.toHome(limit))
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun shakeView(view: View) {
        val shake =
            android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        view.startAnimation(shake)
    }

    private fun setupRecyclerView() {
        // 2 ustunli to'r: budjet plitkalari + "New budget" + "Smart tip"
        categoryAdapter = BudgetTileAdapter(
            onDeleteClick = { budget -> showDeleteConfirmation(budget) },
            onAddClick = { showAddBudgetDialog() }
        )
        val spacing = (12 * resources.displayMetrics.density).toInt()
        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = categoryAdapter
            isNestedScrollingEnabled = false
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val position = parent.getChildAdapterPosition(view)
                    if (position == RecyclerView.NO_POSITION) return
                    val column = position % 2
                    outRect.left = column * spacing / 2
                    outRect.right = spacing - (column + 1) * spacing / 2
                    outRect.top = if (position >= 2) spacing else 0
                }
            })
        }
    }

    private fun showDeleteConfirmation(budget: Budget) {
        MaterialAlertDialogBuilder(requireContext(), R.style.RoundedMaterialDialog)
            .setTitle("Delete Budget")
            .setMessage("Are you sure you want to delete \"${budget.title}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteBudget(budget)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private var isEditMode = false

    private fun setupEditButton() {
        binding.btnEditCategories.setOnClickListener {
            isEditMode = !isEditMode
            categoryAdapter.setEditMode(isEditMode)
            binding.btnEditCategories.text = if (isEditMode) "Done" else "Edit"
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        bindUiState(state)
                    }
                }
            }
        }
    }

    /**
     * Holat rangi foizga qarab (finara_colors.xml):
     * 80% gacha - yashil (On track), 80-99% - sariq (Near limit), 100%+ - qizil (Over budget).
     * Biror kategoriya limitdan oshgan bo'lsa - umumiy foizdan qat'i nazar qizil.
     */
    private fun applyStatusColors(percent: Int, overBudgetCount: Int) {
        val (main, soft) = when {
            overBudgetCount > 0 || percent >= 100 -> R.color.fin_error to R.color.fin_error_tint
            percent < 80 -> R.color.fin_success to R.color.fin_success_tint
            percent < 100 -> R.color.fin_warning to R.color.fin_warning_tint
            else -> R.color.fin_error to R.color.fin_error_tint
        }
        binding.chipStatus.setTextColor(color(main))
        binding.chipStatus.chipIconTint = ColorStateList.valueOf(color(main))
        binding.chipStatus.chipBackgroundColor = ColorStateList.valueOf(color(soft))
        // Ikonka: hammasi joyida bo'lsa ✓, aks holda ⓘ
        binding.chipStatus.setChipIconResource(
            if (overBudgetCount == 0 && percent < 80) R.drawable.ic_check else R.drawable.ic_info
        )
    }

    private fun color(res: Int) = ContextCompat.getColor(requireContext(), res)

    @SuppressLint("SetTextI18n")
    private fun bindUiState(state: BudgetUiState) {
        binding.bgHeaderSubtitle.text = state.monthLabel

        // Umumiy karta: budjet bo'lmasa yashiriladi (to'rda faqat "New budget" qoladi)
        val hasBudgets = state.categories.isNotEmpty()
        binding.budgetOverview.visibility = if (hasBudgets) View.VISIBLE else View.GONE
        if (hasBudgets) {
            val over = state.remaining < 0
            binding.tvRemainingLabel.setText(if (over) R.string.budget_over_by else R.string.budget_left_to_spend)
            binding.tvRemaining.text = formatAmount(kotlin.math.abs(state.remaining))
            binding.tvRemaining.setTextColor(color(if (over) R.color.fin_error else R.color.fin_navy))
            binding.tvDailyLeft.text = formatAmount(state.dailyLeft.coerceAtLeast(0.0))

            binding.chipStatus.text = state.statusText
            applyStatusColors(state.spentPercent, state.overBudgetCount)
            bindShareBar(state)
        }

        // Smart tip matni: summalar shu yerda valyutada formatlanadi
        val tip = when (val t = state.smartTip) {
            SmartTip.NoBudgets -> getString(R.string.budget_tip_no_budgets)
            SmartTip.AllGood -> getString(R.string.budget_tip_all_good)
            is SmartTip.OverBudget ->
                getString(R.string.budget_tip_over, t.category, formatAmount(t.overAmount))
            is SmartTip.NearLimit -> getString(
                R.string.budget_tip_near_limit,
                formatAmount(t.left), t.category, formatAmount(t.perDay)
            )
        }

        categoryAdapter.updateData(state.categories, tip)
    }

    /**
     * Taqsimot chizig'i: har bir kategoriyaning xarajati o'z rangida, oxirida "qolgan" qism.
     * Kenglik = xarajat / max(umumiy limit, umumiy xarajat). Legenda har segment uchun.
     */
    private fun bindShareBar(state: BudgetUiState) {
        val palette = listOf(
            R.color.fin_blue, R.color.fin_orange, R.color.fin_success,
            R.color.fin_warning, R.color.fin_navy, R.color.fin_text_secondary
        )
        val total = maxOf(state.totalLimit, state.totalSpent).takeIf { it > 0 } ?: return
        val gap = (3 * resources.displayMetrics.density).toInt()

        binding.shareBar.removeAllViews()
        binding.legendGroup.removeAllViews()

        state.categories.filter { it.spentAmount > 0 }.forEachIndexed { index, budget ->
            val colorRes = palette[index % palette.size]
            addSegment(budget.spentAmount / total, color(colorRes), gap)
            addLegend("${budget.title} ${formatAmount(budget.spentAmount)}", color(colorRes))
        }
        if (state.remaining > 0) {
            addSegment(state.remaining / total, color(R.color.fin_blue_tint), gap)
            addLegend(getString(R.string.budget_left_segment), color(R.color.fin_border))
        }
    }

    private fun addSegment(fraction: Double, color: Int, gap: Int) {
        val segment = View(requireContext()).apply { setBackgroundColor(color) }
        val params =
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, fraction.toFloat())
        if (binding.shareBar.childCount > 0) params.marginStart = gap
        binding.shareBar.addView(segment, params)
    }

    private fun addLegend(text: String, dotColor: Int) {
        val dot =
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_legend_dot)?.mutate()?.apply {
                setTint(dotColor)
                setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            }
        val label = TextView(requireContext()).apply {
            this.text = text
            textSize = 12f
            setTextColor(color(R.color.fin_text_secondary))
            typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
            gravity = Gravity.CENTER_VERTICAL
            compoundDrawablePadding = (6 * resources.displayMetrics.density).toInt()
            setCompoundDrawablesRelative(dot, null, null, null)
        }
        binding.legendGroup.addView(label)
    }
}