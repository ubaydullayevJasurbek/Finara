package com.ubaydullayev.expensetracker.presentation.savinggoals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat.isNestedScrollingEnabled
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ubaydullayev.expensetracker.adapter.savinggoaladapter.ActiveGoalsAdapter
import com.ubaydullayev.expensetracker.adapter.savinggoaladapter.CompletedGoalsAdapter
import com.ubaydullayev.expensetracker.adapter.savinggoaladapter.PlannedGoalsAdapter
import com.ubaydullayev.expensetracker.data.local.database.AppDatabase
import com.ubaydullayev.expensetracker.data.repository.ContributionRepositoryImpl
import com.ubaydullayev.expensetracker.data.repository.SavingGoalRepositoryImpl
import com.ubaydullayev.expensetracker.databinding.ScreenSavingGoalsBinding
import com.ubaydullayev.expensetracker.presentation.common.BaseViewModelFragment
import com.ubaydullayev.expensetracker.utils.MonthlyContribution
import kotlinx.coroutines.launch

class SavingGoalsScreen :
    BaseViewModelFragment<ScreenSavingGoalsBinding, SavingGoalsViewModel>(SavingGoalsViewModel::class.java) {

    override val viewModelFactory: ViewModelProvider.Factory
        get() {
            val database = AppDatabase.getInstance(requireContext())
            return SavingGoalsViewModelFactory(
                savingGoalRepository = SavingGoalRepositoryImpl(database.savingGoalDao()),
                contributionRepository = ContributionRepositoryImpl(database.contributionDao())
            )
        }

    private lateinit var activeAdapter: ActiveGoalsAdapter
    private lateinit var completedAdapter: CompletedGoalsAdapter
    private lateinit var plannedAdapter: PlannedGoalsAdapter

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenSavingGoalsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeData()
    }

    private fun setupRecyclerViews() {
        activeAdapter = ActiveGoalsAdapter(emptyList())
        binding.rvActiveGoals.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = activeAdapter
            isNestedScrollingEnabled = false
        }

        completedAdapter = CompletedGoalsAdapter(emptyList())
        binding.rvCompletedGoals.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = completedAdapter
            isNestedScrollingEnabled = false
        }

        plannedAdapter = PlannedGoalsAdapter(emptyList())
        binding.rvPlannedGoals.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = plannedAdapter
            isNestedScrollingEnabled = false
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
                launch {
                    viewModel.monthlyContributions.collect { monthly ->
                        bindContributionChart(monthly)
                    }
                }
                launch {
                    viewModel.averageMonthlyContribution.collect { avg ->
                        binding.tvAvgContribution.text = "$${String.format("%,.0f", avg)}"
                    }
                }
            }
        }
    }

    private fun bindUiState(state: SavingGoalsUiState) {
        binding.tvHeaderSubtitle.text =
            "${state.activeCount} active · ${state.completedCount} completed"

        binding.tvTotalSavedAmount.text = "$${String.format("%,.0f", state.totalSaved)}"
        binding.tvTargetSubtitle.text = "of $${String.format("%,.0f", state.totalTarget)} target"
        binding.overallProgressIndicator.progress = state.overallPercent
        binding.tvOverallPercent.text = "${state.overallPercent}%"

        binding.tvMilestoneValue.text =
            "$${
                String.format(
                    "%,.0f",
                    state.milestoneRemaining
                )
            } more to reach $${state.nextMilestoneTarget} saved"

        activeAdapter.updateData(state.activeGoals)
        completedAdapter.updateData(state.completedGoals)
        plannedAdapter.updateData(state.plannedGoals)

        binding.sectionCompleted.visibility =
            if (state.completedGoals.isEmpty()) View.GONE else View.VISIBLE
        binding.sectionPlanned.visibility =
            if (state.plannedGoals.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun bindContributionChart(monthly: List<MonthlyContribution>) {
        if (monthly.size < 6) return

        val bars = listOf(
            binding.bar1 to binding.tvBarLabel1,
            binding.bar2 to binding.tvBarLabel2,
            binding.bar3 to binding.tvBarLabel3,
            binding.bar4 to binding.tvBarLabel4,
            binding.bar5 to binding.tvBarLabel5,
            binding.bar6 to binding.tvBarLabel6
        )

        val maxAmount = monthly.maxOfOrNull { it.totalAmount }?.takeIf { it > 0 } ?: 1.0
        val maxHeightDp = 90
        val density = resources.displayMetrics.density

        bars.forEachIndexed { index, (barView, labelView) ->
            val data = monthly[index]
            labelView.text = data.monthLabel

            val heightDp = ((data.totalAmount / maxAmount) * maxHeightDp).coerceAtLeast(8.0)
            val heightPx = (heightDp * density).toInt()

            val params = barView.layoutParams
            params.height = heightPx
            barView.layoutParams = params

            val isLastMonth = index == bars.lastIndex
            barView.setBackgroundResource(
                if (isLastMonth) com.ubaydullayev.expensetracker.R.drawable.bg_contribution_bar_active
                else com.ubaydullayev.expensetracker.R.drawable.bg_contribution_bar_inactive
            )
        }
    }
}