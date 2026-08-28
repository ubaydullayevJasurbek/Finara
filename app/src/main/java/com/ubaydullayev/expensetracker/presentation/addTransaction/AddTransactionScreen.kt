package com.ubaydullayev.expensetracker.presentation.addTransaction

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.databinding.ScreenAddTransactionBinding
import com.ubaydullayev.expensetracker.presentation.common.BaseFragment
import kotlin.getValue
import androidx.core.graphics.toColorInt
import androidx.navigation.fragment.findNavController
import com.ubaydullayev.expensetracker.presentation.addTransaction.categorysheet.CategoryPickerSheet

class AddTransactionScreen : BaseFragment<ScreenAddTransactionBinding>() {

    private val args: AddTransactionScreenArgs by navArgs()

    private var selectedType: String = "Expense"

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenAddTransactionBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         selectedType = args.transactionType

        setupTypeTabs()
        setupAmountInput()
        setupCloseButton()
        setupCategoryPicker()

    }

    private fun setupCategoryPicker() {
        binding.rowCategory.setOnClickListener {
            CategoryPickerSheet().show(childFragmentManager,"CategoryPickerSheet")
        }

        childFragmentManager.setFragmentResultListener(
            CategoryPickerSheet.REQUEST_KEY,
            viewLifecycleOwner
        ){_,bundle ->

            val category = bundle.getString(CategoryPickerSheet.RESULT_CATEGORY)?:return@setFragmentResultListener
            applyCategorySelection(category)
        }

    }

    private fun applyCategorySelection(category: String){
        binding.tvCategoryValue.text = category
        binding.tvCategoryValue.setTextColor("#1A1A1A".toColorInt())

        val style = com.ubaydullayev.expensetracker.utils.CategoryStyleProvider.getStyle(category)
        val icon = binding.categoryIconBg.getChildAt(0) as android.widget.ImageView
        icon.setImageResource(style.iconRes)

        val bg = android.graphics.drawable.GradientDrawable()
        bg.shape = android.graphics.drawable.GradientDrawable.OVAL
        bg.setColor(style.iconBgColor)
        binding.categoryIconBg.background = bg
    }

    private fun setupTypeTabs(){
        binding.tabExpense.setOnClickListener { applyTypeSelection("Expense") }
        binding.tabIncome.setOnClickListener {  applyTypeSelection("Income")}
        binding.tabTransfer.setOnClickListener {  applyTypeSelection("Transfer")}
    }

    private fun applyTypeSelection(type: String){

        binding.tabExpense.background = null
        binding.tabIncome.background = null
        binding.tabTransfer.background = null

        binding.tabExpense.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
        binding.tabIncome.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
        binding.tabTransfer.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))


        val selectedTab = when(type){
            "Expense" -> binding.tabExpense
            "Income" -> binding.tabIncome
            else -> binding.tabTransfer
        }

        selectedTab.setBackgroundResource(R.drawable.bg_segment_selected)

        val selectedTextColor = when(type){
            "Expense" -> "#B71C1C"
            "Income" -> "#1B5E20"
            else -> "#0D47A1"
        }

        selectedTab.setTextColor(selectedTextColor.toColorInt())


        binding.tvScreenTitle.text = when(type){
            "Expense" -> getString(R.string.new_expense)
            "Income" -> getString(R.string.new_income)
            else -> getString(R.string.new_transfer)
        }
    }

    private fun setupAmountInput(){

        binding.etAmount.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Hozircha faqat raqam kiritishga ruxsat beramiz — maxsus formatlash keyinroq
            }
        })
    }

    private fun getEnteredAmount(): Double {
        val text = binding.etAmount.text.toString()
        return text.toDoubleOrNull() ?: 0.0
    }

    private fun setupCloseButton(){
        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}