package com.ubaydullayev.expensetracker.presentation.addmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.data.local.database.AppDatabase
import com.ubaydullayev.expensetracker.data.local.mapper.toDomainModel
import com.ubaydullayev.expensetracker.data.repository.ContributionRepositoryImpl
import com.ubaydullayev.expensetracker.data.repository.SavingGoalRepositoryImpl
import com.ubaydullayev.expensetracker.databinding.ScreenAddMoneyToGoalBinding
import com.ubaydullayev.expensetracker.domain.model.Contribution
import com.ubaydullayev.expensetracker.domain.model.GoalStatus
import com.ubaydullayev.expensetracker.domain.model.SavingGoal
import com.ubaydullayev.expensetracker.utils.CurrencyFormatter
import com.ubaydullayev.expensetracker.utils.formatAmount
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.Locale

/**
 * Maqsadga pul qo'shish: ixcham bottom sheet.
 * Maqsad chiplardan tanlanadi, summa yoziladi yoki +10/+50/+100 bilan to'ldiriladi.
 */
class AddMoneyToGoalScreen : BottomSheetDialogFragment() {

    private var _binding: ScreenAddMoneyToGoalBinding? = null
    private val binding get() = _binding!!

    private var goals: List<SavingGoal> = emptyList()
    private var selectedGoal: SavingGoal? = null

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ScreenAddMoneyToGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvAmountCurrency.text = CurrencyFormatter.current.symbol
        setupQuickAmounts()
        binding.etGoalAmount.doAfterTextChanged { binding.tvAmountError.visibility = View.GONE }
        binding.btnConfirmAddMoney.setOnClickListener { confirm() }
        loadGoals()
    }

    override fun onStart() {
        super.onStart()
        // Klaviatura ochilganda tugma ko'rinib tursin; sheet darhol to'liq ochilsin
        (dialog as? BottomSheetDialog)?.let { sheet ->
            sheet.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            sheet.behavior.skipCollapsed = true
            sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    // ---------- Maqsadlar ----------

    private fun loadGoals() {
        val dao = AppDatabase.getInstance(requireContext()).savingGoalDao()
        viewLifecycleOwner.lifecycleScope.launch {
            goals = dao.getActiveGoals().first().map { it.toDomainModel() }
            renderGoals()
        }
    }

    private fun renderGoals() {
        val hasGoals = goals.isNotEmpty()
        binding.goalsScroll.visibility = if (hasGoals) View.VISIBLE else View.GONE
        binding.tvNoGoals.visibility = if (hasGoals) View.GONE else View.VISIBLE
        binding.btnConfirmAddMoney.isEnabled = hasGoals

        binding.chipGroupGoals.removeAllViews()
        goals.forEach { goal -> binding.chipGroupGoals.addView(createGoalChip(goal)) }

        // Bitta maqsad bo'lsa, darhol tanlanadi
        if (goals.size == 1) (binding.chipGroupGoals.getChildAt(0) as Chip).isChecked = true
    }

    private fun createGoalChip(goal: SavingGoal): Chip {
        val ctx = requireContext()
        return Chip(ctx).apply {
            id = View.generateViewId()
            text = goal.title
            isCheckable = true
            isCheckedIconVisible = false
            chipBackgroundColor = ContextCompat.getColorStateList(ctx, R.color.chip_bg_selector)
            setTextColor(ContextCompat.getColorStateList(ctx, R.color.chip_text_selector))
            chipStrokeColor = ContextCompat.getColorStateList(ctx, R.color.chip_stroke_selector)
            chipStrokeWidth = resources.displayMetrics.density
            chipCornerRadius = 20f * resources.displayMetrics.density
            typeface = ResourcesCompat.getFont(ctx, R.font.inter_medium)
            setOnCheckedChangeListener { _, checked -> if (checked) selectGoal(goal) }
        }
    }

    private fun selectGoal(goal: SavingGoal) {
        selectedGoal = goal
        val left = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
        binding.tvGoalProgress.text = getString(
            R.string.add_money_progress,
            formatAmount(goal.currentAmount),
            formatAmount(goal.targetAmount),
            formatAmount(left)
        )
        binding.tvGoalProgress.setTextColor(ContextCompat.getColor(requireContext(), R.color.fin_text_secondary))
        binding.tvGoalProgress.visibility = View.VISIBLE
    }

    // ---------- Summa ----------

    /** So'm kabi kasrsiz valyutalarda tez qo'shish summalari kattaroq. */
    private fun quickSteps(): List<Long> =
        if (CurrencyFormatter.current.decimals == 0) listOf(10_000, 50_000, 100_000)
        else listOf(10, 50, 100)

    private fun setupQuickAmounts() {
        val buttons = listOf(binding.btnQuick10, binding.btnQuick50, binding.btnQuick100)
        val currency = CurrencyFormatter.current
        val grouping = DecimalFormat("#,##0")
        buttons.zip(quickSteps()).forEach { (button, step) ->
            val number = grouping.format(step)
            button.text = if (currency.symbolAfter) "+$number ${currency.symbol}" else "+${currency.symbol}$number"
            button.setOnClickListener { addToAmount(step) }
        }
    }

    private fun addToAmount(step: Long) {
        val current = binding.etGoalAmount.text.toString().toDoubleOrNull() ?: 0.0
        val next = current + step
        val text = if (next % 1.0 == 0.0) next.toLong().toString() else String.format(Locale.US, "%.2f", next)
        binding.etGoalAmount.setText(text)
        binding.etGoalAmount.setSelection(text.length)
    }

    // ---------- Saqlash ----------

    private fun confirm() {
        val goal = selectedGoal
        if (goal == null) {
            binding.tvGoalProgress.text = getString(R.string.add_money_choose_goal)
            binding.tvGoalProgress.setTextColor(ContextCompat.getColor(requireContext(), R.color.fin_error))
            binding.tvGoalProgress.visibility = View.VISIBLE
            shake(binding.goalsScroll)
            return
        }

        val amount = binding.etGoalAmount.text.toString().toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            binding.tvAmountError.visibility = View.VISIBLE
            shake(binding.amountBox)
            return
        }

        binding.btnConfirmAddMoney.isEnabled = false
        saveContribution(goal, CurrencyFormatter.toHome(amount))
    }

    private fun saveContribution(goal: SavingGoal, amount: Double) {
        val database = AppDatabase.getInstance(requireContext())
        val goalRepository = SavingGoalRepositoryImpl(database.savingGoalDao())
        val contributionRepository = ContributionRepositoryImpl(database.contributionDao())
        val appContext = requireContext().applicationContext

        viewLifecycleOwner.lifecycleScope.launch {
            contributionRepository.addContribution(
                Contribution(
                    id = 0,
                    goalId = goal.id,
                    amount = amount,
                    timestamp = System.currentTimeMillis()
                )
            )
            goalRepository.addToCurrentAmount(goal.id, amount)

            if (goal.currentAmount + amount >= goal.targetAmount) {
                goalRepository.updateStatus(goal.id, GoalStatus.COMPLETED)
            }

            Toast.makeText(appContext, appContext.getString(R.string.add_money_done, goal.title), Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun shake(view: View) {
        view.startAnimation(android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.shake))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
