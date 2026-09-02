package com.ubaydullayev.expensetracker.presentation.savinggoals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ubaydullayev.expensetracker.domain.model.GoalStatus
import com.ubaydullayev.expensetracker.domain.model.SavingGoal
import com.ubaydullayev.expensetracker.domain.repository.ContributionRepository
import com.ubaydullayev.expensetracker.domain.repository.SavingGoalRepository
import com.ubaydullayev.expensetracker.utils.ContributionGrouper
import com.ubaydullayev.expensetracker.utils.MonthlyContribution
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import kotlin.math.ceil

data class SavingGoalsUiState(
    val activeCount: Int = 0,
    val completedCount: Int = 0,
    val totalSaved: Double = 0.0,
    val totalTarget: Double = 0.0,
    val overallPercent: Int = 0,
    val milestoneRemaining: Double = 0.0,
    val nextMilestoneTarget: Int = 0,
    val activeGoals: List<SavingGoal> = emptyList(),
    val completedGoals: List<SavingGoal> = emptyList(),
    val plannedGoals: List<SavingGoal> = emptyList()
)

class SavingGoalsViewModel(
    private val savingGoalRepository: SavingGoalRepository,
    private val contributionRepository: ContributionRepository
) : ViewModel() {

    val uiState: StateFlow<SavingGoalsUiState> =
        savingGoalRepository.getAllGoals()
            .map { allGoals -> buildUiState(allGoals) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SavingGoalsUiState())

    val monthlyContributions: StateFlow<List<MonthlyContribution>> =
        contributionRepository.getContributionsSince(getSixMonthsAgoTimestamp())
            .map { contributions -> ContributionGrouper.groupByLastSixMonths(contributions) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val averageMonthlyContribution: StateFlow<Double> =
        monthlyContributions
            .map { ContributionGrouper.calculateAverage(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private fun buildUiState(allGoals: List<SavingGoal>): SavingGoalsUiState {
        val active = allGoals.filter { it.status == GoalStatus.ACTIVE }
        val completed = allGoals.filter { it.status == GoalStatus.COMPLETED }
        val planned = allGoals.filter { it.status == GoalStatus.PLANNED }

        val totalSaved = active.sumOf { it.currentAmount }
        val totalTarget = active.sumOf { it.targetAmount }
        val percent = if (totalTarget > 0) ((totalSaved / totalTarget) * 100).toInt() else 0

        val nextMilestone = (ceil(totalSaved / 1000.0) * 1000).toInt()
        val remaining = nextMilestone - totalSaved

        return SavingGoalsUiState(
            activeCount = active.size,
            completedCount = completed.size,
            totalSaved = totalSaved,
            totalTarget = totalTarget,
            overallPercent = percent,
            milestoneRemaining = remaining,
            nextMilestoneTarget = nextMilestone,
            activeGoals = active,
            completedGoals = completed,
            plannedGoals = planned
        )
    }

    private fun getSixMonthsAgoTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -5)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return calendar.timeInMillis
    }
}