package com.ubaydullayev.expensetracker.presentation.budget

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.adapter.budgetadapter.BudgetCategoryAdapter
import com.ubaydullayev.expensetracker.data.local.database.AppDatabase
import com.ubaydullayev.expensetracker.data.repository.BudgetRepositoryImpl
import com.ubaydullayev.expensetracker.databinding.DialogAddBudgetBinding
import com.ubaydullayev.expensetracker.databinding.ScreenBudgetBinding
import com.ubaydullayev.expensetracker.presentation.addTransaction.categorysheet.CategoryPickerSheet
import com.ubaydullayev.expensetracker.presentation.common.BaseViewModelFragment
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt
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

    private lateinit var categoryAdapter: BudgetCategoryAdapter

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenBudgetBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEditButton()
        setupAddBudgetButton()
        setupRecyclerView()
        observeData()
    }

    private fun setupAddBudgetButton() {
        binding.btnBudget.setOnClickListener {
            showAddBudgetDialog()
        }
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
            dialogBinding.tvSelectedBudgetCategory.setTextColor("#1A1A1A".toColorInt())
        }

        dialog.setOnShowListener {
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.setTextColor("#0F7B55".toColorInt())

            val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            cancelButton.setTextColor("#9E9E9E".toColorInt())

            addButton.setOnClickListener {
                val category = selectedCategory
                val limitText = dialogBinding.etBudgetLimit.text.toString()
                val limit = limitText.toDoubleOrNull()

                when {
                    category == null -> {
                        shakeView(dialogBinding.rowSelectBudgetCategory)
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
        categoryAdapter = BudgetCategoryAdapter(emptyList()) { budget ->
            showDeleteConfirmation(budget)
        }
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
            isNestedScrollingEnabled = false
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

    @SuppressLint("SetTextI18n")
    private fun bindUiState(state: BudgetUiState) {
        // Header
        binding.bgHeaderSubtitle.text = state.monthLabel

        // Overview hero
        binding.tvSpentAmount.text = formatAmount(state.totalSpent)
        binding.tvTotalAmount.text = "of ${formatAmount(state.totalLimit)}"
        binding.progressSpent.progress = state.spentPercent.coerceAtMost(100)

        binding.tvRemaining.text = formatAmount(state.remaining)
        binding.tvDailyLeft.text = formatAmount(state.dailyLeft)

        binding.chipStatus.text = state.statusText

        // Smart Tip
        binding.tvSmartTip.text = state.smartTipCategory?.let { category ->
            "Reduce $category by ${formatAmount(state.smartTipReduceBy)} to stay under budget this month."
        } ?: state.smartTipText

        // Categories
        categoryAdapter.updateData(state.categories)
    }
}
