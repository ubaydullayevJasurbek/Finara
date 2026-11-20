package com.ubaydullayev.expensetracker.presentation.transaction

import android.annotation.SuppressLint
import android.graphics.Outline
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.adapter.transactionadapter.GroupedTransactionsAdapter
import com.ubaydullayev.expensetracker.data.local.database.AppDatabase
import com.ubaydullayev.expensetracker.data.repository.TransactionRepositoryImpl
import com.ubaydullayev.expensetracker.databinding.ScreenTransactionBinding
import com.ubaydullayev.expensetracker.presentation.common.BaseViewModelFragment
import com.ubaydullayev.expensetracker.presentation.common.applySystemBarInsetsAsPadding
import com.ubaydullayev.expensetracker.utils.formatAmount
import kotlinx.coroutines.launch

class TransactionScreen :
    BaseViewModelFragment<ScreenTransactionBinding, TransactionViewModel>(TransactionViewModel::class.java) {

    // Bottom bar activity'da: navigation-bar inset'ini bu ekran qo'shmaydi (HomeScreen kabi).
    override val insetBottom: Boolean = false

    // Hero status bar ostiga cho'ziladi (Home bilan bir xil); ikonkalar oq.
    override val insetTop: Boolean = false
    override val lightStatusBars: Boolean = false

    override val viewModelFactory: ViewModelProvider.Factory
        get() {
            val database = AppDatabase.getInstance(requireContext())
            return TransactionViewModelFactory(
                transactionRepository = TransactionRepositoryImpl(database.transactionDao())
            )
        }

    private lateinit var transactionsAdapter: GroupedTransactionsAdapter

    private val chips by lazy {
        mapOf(
            TransactionFilter.ALL to binding.chipAll,
            TransactionFilter.INCOME to binding.chipIncome,
            TransactionFilter.EXPENSE to binding.chipExpense,
            TransactionFilter.RECURRING to binding.chipRecurring,
        )
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenTransactionBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHero()
        setupRecyclerView()
        setupSearch()
        setupClickListeners()
        observeData()
    }

    private fun setupHero() {
        // Girih naqshi pastki yumaloq burchaklardan chiqmasin (HomeScreen bilan bir xil)
        val radius = 30f * resources.displayMetrics.density
        binding.heroContainer.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, -radius.toInt(), view.width, view.height, radius)
            }
        }
        binding.heroContainer.clipToOutline = true
        binding.heroContainer.applySystemBarInsetsAsPadding(
            applyLeft = false,
            applyTop = true,
            applyRight = false,
            applyBottom = false,
        )
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

    private fun setupSearch() {
        binding.etSearchTransactions.doAfterTextChanged { text ->
            viewModel.setQuery(text?.toString().orEmpty())
        }
        // Klaviaturadagi "Search" tugmasi: klaviaturani yopadi (natija allaqachon filtrlangan)
        binding.etSearchTransactions.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm = requireContext().getSystemService(InputMethodManager::class.java)
                imm?.hideSoftInputFromWindow(v.windowToken, 0)
                v.clearFocus()
                true
            } else {
                false
            }
        }
    }

    private fun setupClickListeners() {
        chips.forEach { (filter, chip) ->
            chip.setOnClickListener { viewModel.selectFilter(filter) }
        }
        binding.btnPrevMonth.setOnClickListener { viewModel.previousMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth() }
    }

    @SuppressLint("SetTextI18n")
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.groupedTransactions.collect { items ->
                        transactionsAdapter.updateData(items)
                        binding.tvEmptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.monthSummary.collect { summary ->
                        binding.tvTotalIncome.text = formatAmount(summary.income)
                        binding.tvTotalExpense.text = formatAmount(summary.expense)
                        val sign = if (summary.net >= 0) "+" else "−"
                        binding.tvNetAmount.text = sign + formatAmount(kotlin.math.abs(summary.net))
                    }
                }
                launch {
                    viewModel.monthLabel.collect { binding.monthTransYear.text = it }
                }
                launch {
                    viewModel.monthOffset.collect { offset ->
                        // Kelajak oyga o'tib bo'lmaydi: joriy oyda "keyingi" tugmasi o'chiq
                        val canGoNext = offset < 0
                        binding.btnNextMonth.isEnabled = canGoNext
                        binding.btnNextMonth.alpha = if (canGoNext) 1f else 0.35f
                    }
                }
                launch {
                    viewModel.selectedFilter.collect { updateChipSelection(it) }
                }
            }
        }
    }

    private fun updateChipSelection(selected: TransactionFilter) {
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val navy = ContextCompat.getColor(requireContext(), R.color.fin_navy)
        chips.forEach { (filter, chip) ->
            val active = filter == selected
            chip.setBackgroundResource(if (active) R.drawable.bg_fin_chip_selected else R.drawable.bg_fin_chip_unselected)
            chip.setTextColor(if (active) white else navy)
            chip.isSelected = active
        }
    }
}