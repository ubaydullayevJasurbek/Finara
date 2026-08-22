package com.ubaydullayev.expensetracker.presentation.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ubaydullayev.expensetracker.adapter.transactionadapter.TransactionsAdapter
import com.ubaydullayev.expensetracker.adapter.upcomingbillsadapter.UpcomingBillsAdapter
import com.ubaydullayev.expensetracker.data.local.database.AppDatabase
import com.ubaydullayev.expensetracker.data.repository.SavingGoalRepositoryImpl
import com.ubaydullayev.expensetracker.data.repository.TransactionRepositoryImpl
import com.ubaydullayev.expensetracker.data.repository.UpcomingBillRepositoryImpl
import com.ubaydullayev.expensetracker.databinding.ScreenHomeBinding
import com.ubaydullayev.expensetracker.domain.model.SavingGoal
import com.ubaydullayev.expensetracker.presentation.common.BaseViewModelFragment
import kotlinx.coroutines.launch

class HomeScreen :
    BaseViewModelFragment<ScreenHomeBinding, HomeViewModel>(HomeViewModel::class.java) {

    // The bottom bar now lives in the activity shell (below this fragment) and consumes the
    // navigation-bar inset itself, so Home must not add that inset again or a gap appears above the bar.
    override val insetBottom: Boolean = false

    override val viewModelFactory: ViewModelProvider.Factory
        get() {
            val database = AppDatabase.getInstance(requireContext())
            return HomeViewModelFactory(
                transactionRepository = TransactionRepositoryImpl(database.transactionDao()),
                upcomingBillRepository = UpcomingBillRepositoryImpl(database.upcomingBillDao()),
                savingGoalRepository = SavingGoalRepositoryImpl(database.savingGoalDao())
            )
        }

    private lateinit var transactionsAdapter: TransactionsAdapter
    private lateinit var upcomingBillsAdapter: UpcomingBillsAdapter


    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenHomeBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeData()
    }

    private fun setupRecyclerViews() {
        transactionsAdapter = TransactionsAdapter(emptyList())
        binding.rvRecentTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionsAdapter
            isNestedScrollingEnabled = false
        }

        upcomingBillsAdapter = UpcomingBillsAdapter(emptyList())
        binding.rvUpcomingBills.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = upcomingBillsAdapter
            isNestedScrollingEnabled = false
        }

        setupGoalCardClickListener()
    }


    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle((Lifecycle.State.STARTED)) {
                launch {
                    viewModel.recentTransactions.collect { transactions ->
                        binding.sectionRecentTransactions.visibility =
                            if (transactions.isEmpty()) View.GONE else View.VISIBLE
                        transactionsAdapter.updateData(transactions)
                    }
                }

                launch {
                    viewModel.upcomingBill.collect { upcomingBills ->
                        binding.sectionUpcomingBills.visibility =
                            if (upcomingBills.isEmpty()) View.GONE else View.VISIBLE
                        upcomingBillsAdapter.updateData(upcomingBills)
                    }
                }
                launch {
                    viewModel.savingGoals.collect { savingGoals ->
                        val topGoal = savingGoals.firstOrNull()
                        binding.sectionSavingGoals.visibility =
                            if (topGoal == null) View.GONE else View.VISIBLE
                        if (topGoal != null) bindGoalCard(topGoal)
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindGoalCard(goal: SavingGoal) {
        val cardBinding = binding.includeGoalCard
        cardBinding.tvGoalName.text = goal.title
        cardBinding.tvCategoryTag.text = goal.category
        cardBinding.tvEstDate.text = goal.estimatedDate
        cardBinding.tvPriority.text = goal.priority
        cardBinding.tvTargetDate.text = goal.targetDate

        val percent = ((goal.currentAmount / goal.targetAmount) * 100).toInt()
        cardBinding.tvPercent.text = "$percent%"
        cardBinding.goalProgressRing.progress = percent

        cardBinding.tvSaved.text = "$${formatAmount(goal.currentAmount)}"
        cardBinding.tvTargetAmount.text = "/ $${formatAmount(goal.targetAmount)}"

        val remaining = goal.targetAmount - goal.currentAmount
        cardBinding.tvToGo.text = "$${formatAmount(remaining)} to go"

        val fillParams = cardBinding.goalProgressFill.layoutParams as ConstraintLayout.LayoutParams
        fillParams.matchConstraintPercentWidth = percent / 100f
        cardBinding.goalProgressFill.layoutParams = fillParams
    }

    @SuppressLint("DefaultLocale")
    private fun formatAmount(amount: Double): String {
        return String.format("%,.0f", amount)
    }

    private fun setupGoalCardClickListener() {
        // Kartaning o'ziga bosilganda -> to'liq Saving Goals screen'iga o'tish
        binding.includeGoalCard.root.setOnClickListener {
            // Masalan: findNavController().navigate(R.id.action_home_to_savingGoals)
        }

        // "See all" tugmasiga bosilganda ham xuddi shu screen'ga o'tish
        binding.btnSeeAllSavingGoals.setOnClickListener {
            // findNavController().navigate(R.id.action_home_to_savingGoals)
        }
    }
}
