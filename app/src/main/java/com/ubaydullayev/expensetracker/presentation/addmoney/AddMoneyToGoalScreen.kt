package com.ubaydullayev.expensetracker.presentation.addmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.data.local.database.AppDatabase
import com.ubaydullayev.expensetracker.data.local.mapper.toDomainModel
import com.ubaydullayev.expensetracker.data.repository.ContributionRepositoryImpl
import com.ubaydullayev.expensetracker.data.repository.SavingGoalRepositoryImpl
import com.ubaydullayev.expensetracker.databinding.DialogSelectGoalBinding
import com.ubaydullayev.expensetracker.databinding.ItemSelectGoalBinding
import com.ubaydullayev.expensetracker.databinding.ScreenAddMoneyToGoalBinding
import com.ubaydullayev.expensetracker.domain.model.Contribution
import com.ubaydullayev.expensetracker.domain.model.GoalStatus
import com.ubaydullayev.expensetracker.domain.model.SavingGoal
import com.ubaydullayev.expensetracker.presentation.common.BaseFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddMoneyToGoalScreen : BaseFragment<ScreenAddMoneyToGoalBinding>() {

    private var selectedGoal: SavingGoal? = null

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenAddMoneyToGoalBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCloseButton()
        setupGoalSelector()
        setupConfirmButton()
    }

    private fun setupCloseButton() {
        binding.btnCloseAddMoney.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupGoalSelector() {
        binding.rowSelectGoal.setOnClickListener {
            showGoalPickerDialog()
        }
    }

    private fun showGoalPickerDialog() {
        val dao = AppDatabase.getInstance(requireContext()).savingGoalDao()

        viewLifecycleOwner.lifecycleScope.launch {
            val activeGoals = dao.getActiveGoals().first().map { it.toDomainModel() }

            if (activeGoals.isEmpty()) {
                MaterialAlertDialogBuilder(requireContext(), R.style.RoundedMaterialDialog)
                    .setTitle("No Active Goals")
                    .setMessage("You don't have any active saving goals yet. Create one first.")
                    .setPositiveButton("OK", null)
                    .show()
                return@launch
            }

            val dialogBinding = DialogSelectGoalBinding.inflate(layoutInflater)
            val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.RoundedMaterialDialog)
                .setView(dialogBinding.root)
                .create()

            dialogBinding.rvGoalList.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = GoalPickerAdapter(activeGoals) { goal ->
                    selectedGoal = goal
                    binding.tvSelectedGoalName.text = goal.title
                    binding.tvSelectedGoalName.setTextColor("#1A1A1A".toColorInt())
                    dialog.dismiss()
                }
            }

            dialog.show()
        }
    }

    private fun setupConfirmButton() {
        binding.btnConfirmAddMoney.setOnClickListener {
            val goal = selectedGoal
            if (goal == null) {
                shakeView(binding.rowSelectGoal)
                return@setOnClickListener
            }

            val amountText = binding.etGoalAmount.text.toString()
            val amount = amountText.toDoubleOrNull()

            if (amount == null || amount <= 0.0) {
                binding.etGoalAmount.error = "Enter a valid amount"
                shakeView(binding.etGoalAmount)
                return@setOnClickListener
            }

            saveContribution(goal, amount)
        }
    }

    private fun saveContribution(goal: SavingGoal, amount: Double) {
        val database = AppDatabase.getInstance(requireContext())
        val goalRepository = SavingGoalRepositoryImpl(database.savingGoalDao())
        val contributionRepository = ContributionRepositoryImpl(database.contributionDao())

        viewLifecycleOwner.lifecycleScope.launch {
            // 1. Kontribyutsiya tarixini yozamiz
            contributionRepository.addContribution(
                Contribution(
                    id = 0,
                    goalId = goal.id,
                    amount = amount,
                    timestamp = System.currentTimeMillis()
                )
            )

            // 2. Goal'ning joriy summasini oshiramiz
            goalRepository.addToCurrentAmount(goal.id, amount)

            // 3. Agar maqsadga yetgan bo'lsa, statusni COMPLETED qilamiz
            val newAmount = goal.currentAmount + amount
            if (newAmount >= goal.targetAmount) {
                goalRepository.updateStatus(goal.id, GoalStatus.COMPLETED)
            }

            findNavController().popBackStack(R.id.homeScreen, false)
        }
    }

    private fun shakeView(view: View) {
        val shake = android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        view.startAnimation(shake)
    }
}
