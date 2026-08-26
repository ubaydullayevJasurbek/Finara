package com.ubaydullayev.expensetracker.presentation.addfab

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.databinding.ScreenAddFabBinding
import androidx.core.graphics.toColorInt
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

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


    @SuppressLint("SetTextI18n")
    private fun setupCards() {
        // Expense
        binding.cardExpense.optionCard.setCardBackgroundColor("#E53935".toColorInt())
        binding.cardExpense.optionIcon.setImageResource(R.drawable.ic_arrow_down)
        binding.cardExpense.optionTitle.text = "Expense"
        binding.cardExpense.optionSubtitle.text = "Track spending"

        // Income
        binding.cardIncome.optionCard.setCardBackgroundColor("#2E7D32".toColorInt())
        binding.cardIncome.optionIcon.setImageResource(R.drawable.ic_arrow_up)
        binding.cardIncome.optionTitle.text = "Income"
        binding.cardIncome.optionSubtitle.text = "Log earnings"

        // Transfer
        binding.cardTransfer.optionCard.setCardBackgroundColor("#1E88E5".toColorInt())
        binding.cardTransfer.optionIcon.setImageResource(R.drawable.ic_transfer)
        binding.cardTransfer.optionTitle.text = "Transfer"
        binding.cardTransfer.optionSubtitle.text = "Move money"

        // Saving Goal
        binding.cardSavingGoal.optionCard.setCardBackgroundColor("#7C3AED".toColorInt())
        binding.cardSavingGoal.optionIcon.setImageResource(R.drawable.ic_target)
        binding.cardSavingGoal.optionTitle.text = "Saving Goal"
        binding.cardSavingGoal.optionSubtitle.text = "Fund a target"

        // Bill
        binding.cardBill.optionCard.setCardBackgroundColor("#F59E0B".toColorInt())
        binding.cardBill.optionIcon.setImageResource(R.drawable.ic_document)
        binding.cardBill.optionTitle.text = "Bill"
        binding.cardBill.optionSubtitle.text = "Schedule payment"

        // Category
        binding.cardCategory.optionCard.setCardBackgroundColor("#0891B2".toColorInt())
        binding.cardCategory.optionIcon.setImageResource(R.drawable.ic_grid)
        binding.cardCategory.optionTitle.text = "Category"
        binding.cardCategory.optionSubtitle.text = "Create custom"
    }

    private fun setupClickListeners() {

        binding.btnCloseSheet.setOnClickListener {
            dismiss()
        }

        binding.cardExpense.root.setOnClickListener {
            // TODO: Add Transaction — Redesign screen (Expense tab)
        }

        binding.cardIncome.root.setOnClickListener {
            // TODO: Add Transaction — Redesign screen (Income tab)
        }

        binding.cardTransfer.root.setOnClickListener {
            // TODO: hali screen yo'q
        }

        binding.cardSavingGoal.root.setOnClickListener {
            // TODO: hali screen yo'q
        }

        binding.cardBill.root.setOnClickListener {
            // TODO: hali screen yo'q
        }

        binding.cardCategory.root.setOnClickListener {
            // TODO: hali screen yo'q
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}