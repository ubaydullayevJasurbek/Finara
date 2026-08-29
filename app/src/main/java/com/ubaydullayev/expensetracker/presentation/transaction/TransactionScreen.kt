package com.ubaydullayev.expensetracker.presentation.transaction

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.adapter.transactionadapter.GroupedTransactionsAdapter
import com.ubaydullayev.expensetracker.adapter.transactionadapter.TransactionsAdapter
import com.ubaydullayev.expensetracker.data.local.database.AppDatabase
import com.ubaydullayev.expensetracker.data.repository.TransactionRepositoryImpl
import com.ubaydullayev.expensetracker.databinding.ScreenTransactionBinding
import com.ubaydullayev.expensetracker.presentation.common.BaseViewModelFragment
import kotlinx.coroutines.launch
import kotlin.jvm.java

class TransactionScreen :
    BaseViewModelFragment<ScreenTransactionBinding, TransactionViewModel>(TransactionViewModel::class.java) {

    // The bottom bar lives in the activity shell below the nav host, so this screen must not add the
    // navigation-bar inset again (mirrors HomeScreen).
    override val insetBottom: Boolean = false

    override val viewModelFactory: ViewModelProvider.Factory
        get() {
            val database = AppDatabase.getInstance(requireContext())
            return TransactionViewModelFactory(
                transactionRepository = TransactionRepositoryImpl(database.transactionDao())
            )
        }

    private lateinit var transactionsAdapter: GroupedTransactionsAdapter


    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenTransactionBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()
        setupClickListeners()
    }


    private fun setupRecyclerView() {
        transactionsAdapter = GroupedTransactionsAdapter(emptyList()) { transactionId ->

            val action =
                TransactionScreenDirections.actionTransactionScreenToTransactionDetailScreen(
                    transactionId
                )
            findNavController().navigate(action)
        }
        binding.rvAllTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionsAdapter
            isNestedScrollingEnabled = false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.groupedTransactions.collect { transactions ->
                        transactionsAdapter.updateData(transactions)
                    }
                }
                launch {
                    viewModel.monthlyIncome.collect { income ->
                        binding.tvTotalIncome.text = "+$${formatAmount(income)}"
                    }
                }
                launch {
                    viewModel.monthlyExpense.collect { expense ->
                        binding.tvTotalExpense.text = "−$${formatAmount(expense)}"
                    }
                }
                launch {
                    viewModel.selectedFilter.collect { filter ->
                        updateChipSelection(filter)
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.chipAll.setOnClickListener { viewModel.selectFilter(TransactionFilter.ALL) }
        binding.chipToday.setOnClickListener { viewModel.selectFilter(TransactionFilter.TODAY) }
        binding.chipYesterday.setOnClickListener { viewModel.selectFilter(TransactionFilter.YESTERDAY) }
        binding.chipThisWeek.setOnClickListener { viewModel.selectFilter(TransactionFilter.THIS_WEEK) }
        binding.chipThisMonth.setOnClickListener { viewModel.selectFilter(TransactionFilter.THIS_MONTH) }
    }

    private fun updateChipSelection(selected: TransactionFilter) {
        val chips = mapOf(
            TransactionFilter.ALL to binding.chipAll,
            TransactionFilter.TODAY to binding.chipToday,
            TransactionFilter.YESTERDAY to binding.chipYesterday,
            TransactionFilter.THIS_WEEK to binding.chipThisWeek,
            TransactionFilter.THIS_MONTH to binding.chipThisMonth
        )

        chips.forEach { (filter, chip) ->
            if (filter == selected) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
                chip.setTextColor(resources.getColor(R.color.white, null))
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected)
                chip.setTextColor(resources.getColor(R.color.black, null))
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatAmount(amount: Double): String {
        return String.format("%,.0f", amount)
    }
}
