package com.ubaydullayev.expensetracker.presentation.addfab

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.databinding.ItemAddOptionCardBinding
import com.ubaydullayev.expensetracker.databinding.ScreenAddFabBinding

/** "+" menyusi: nima qo'shishni tanlash (Expense, Income, Transfer, Saving goal). */
class AddFabScreen : BottomSheetDialogFragment() {

    private var _binding: ScreenAddFabBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ScreenAddFabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCards()
        setupClickListeners()
    }

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    private fun setupCards() = with(binding) {
        cardExpense.setup(R.drawable.ic_arrow_down, R.string.sheet_add_expense, R.string.sheet_add_expense_sub,
            R.color.fin_error_tint, R.color.fin_error)
        cardIncome.setup(R.drawable.ic_arrow_up, R.string.sheet_add_income, R.string.sheet_add_income_sub,
            R.color.fin_success_tint, R.color.fin_success)
        cardTransfer.setup(R.drawable.ic_transfer, R.string.sheet_add_transfer, R.string.sheet_add_transfer_sub,
            R.color.fin_blue_tint, R.color.fin_blue)
        cardSavingGoal.setup(R.drawable.ic_target, R.string.sheet_add_goal, R.string.sheet_add_goal_sub,
            R.color.fin_orange_tint, R.color.fin_orange_text)
    }

    private fun ItemAddOptionCardBinding.setup(
        @DrawableRes icon: Int,
        @StringRes title: Int,
        @StringRes subtitle: Int,
        @ColorRes tileColor: Int,
        @ColorRes iconColor: Int
    ) {
        val ctx = root.context
        optionIcon.setImageResource(icon)
        optionIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(ctx, iconColor))
        optionIconTile.background = GradientDrawable().apply {
            cornerRadius = 13f * ctx.resources.displayMetrics.density
            setColor(ContextCompat.getColor(ctx, tileColor))
        }
        optionTitle.setText(title)
        optionSubtitle.setText(subtitle)
    }

    private fun setupClickListeners() = with(binding) {
        btnCloseSheet.setOnClickListener { dismiss() }

        // Avval Income va Transfer bosilganda hech narsa bo'lmasdi
        cardExpense.root.setOnClickListener { openAddTransaction("Expense") }
        cardIncome.root.setOnClickListener { openAddTransaction("Income") }
        cardTransfer.root.setOnClickListener { openAddTransaction("Transfer") }

        cardSavingGoal.root.setOnClickListener {
            findNavController().navigate(R.id.action_addFabScreen_to_addMoneyToGoalScreen)
        }
    }

    /** AddTransactionScreen "transactionType" argumentini o'qib, kerakli tabni ochadi. */
    private fun openAddTransaction(type: String) {
        findNavController().navigate(
            R.id.action_addFabScreen_to_addTransactionScreen,
            bundleOf("transactionType" to type)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
