package com.ubaydullayev.expensetracker.presentation.addTransaction

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.data.local.database.AppDatabase
import com.ubaydullayev.expensetracker.data.repository.TransactionRepositoryImpl
import com.ubaydullayev.expensetracker.databinding.DialogAddMerchantBinding
import com.ubaydullayev.expensetracker.databinding.DialogAddNoteBinding
import com.ubaydullayev.expensetracker.databinding.ScreenAddTransactionBinding
import com.ubaydullayev.expensetracker.domain.model.Transaction
import com.ubaydullayev.expensetracker.presentation.addTransaction.categorysheet.CategoryPickerSheet
import com.ubaydullayev.expensetracker.presentation.common.BaseFragment
import com.ubaydullayev.expensetracker.presentation.common.applySystemBarInsetsAsPadding
import com.ubaydullayev.expensetracker.utils.DateFormatter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddTransactionScreen : BaseFragment<ScreenAddTransactionBinding>() {

    private val args: AddTransactionScreenArgs by navArgs()

    private var selectedType: String = "Expense"

    private var selectedTimestamp: Long = System.currentTimeMillis()

    private var enteredNote: String = ""

    // The red header is a full-bleed hero that must draw behind the transparent status bar, so we
    // opt out of BaseFragment's default root inset padding (which would push the header down and
    // leave a white gap) and apply the insets to the right places ourselves in [applyEdgeToEdgeInsets].
    override val applySystemBarInsets: Boolean = false

    // White status-bar icons/time read correctly on the red header.
    override val lightStatusBars: Boolean = false

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenAddTransactionBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedType = args.transactionType

        applyEdgeToEdgeInsets()
        setupTypeTabs()
        setupAmountInput()
        setupCloseButton()
        setupCategoryPicker()
        setupMerchantInput()
        setupDateTimePicker()

        updateDateTimeDisplay()
        setupNoteInput()
        setupAddTransactionButton()
    }

    private fun applyEdgeToEdgeInsets() {
        val panel = binding.amountPanel
        val basePanelHeight = panel.layoutParams.height
        val basePanelContentTop = binding.panelContent.paddingTop

        ViewCompat.setOnApplyWindowInsetsListener(panel) { _, insets ->
            val top = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            ).top
            panel.updateLayoutParams { height = basePanelHeight + top }
            binding.panelContent.updatePadding(top = basePanelContentTop + top)
            insets
        }

        // Keep the panel full-width (no side/top padding here) and only lift the scrollable content
        // above the navigation bar.
        binding.addTransactionScrollView.applySystemBarInsetsAsPadding(
            applyLeft = false,
            applyTop = false,
            applyRight = false,
            applyBottom = true,
        )
    }

    private fun setupAddTransactionButton() {
        binding.btnAddTransaction.setOnClickListener {
            validateAndSaveTransaction()
        }
    }

    private fun validateAndSaveTransaction() {
        //Summa tekshirish
        val amount = getEnteredAmount()

        if (amount <= 0.0) {
            binding.etAmount.error = "Please enter a valid amount"
            shakeView(binding.amountRow)
            return
        }

        // 2. Merchant/title tekshiramiz
        val merchantName = binding.tvMerchantValue.text.toString()
        if (merchantName == getString(R.string.add_merchant)) {
            shakeView(binding.rowMerchant)
            return
        }

        // 3. Kategoriya tekshiramiz
        val categoryName = binding.tvCategoryValue.text.toString()
        if (categoryName == getString(R.string.select_category)) {
            shakeView(binding.rowCategory)
            return
        }

        val transaction = Transaction(
            id = 0,
            title = merchantName,
            category = categoryName,
            timestamp = selectedTimestamp,
            amount = amount,
            isIncome = selectedType == "Income",
            isRecurring = binding.switchRecurring.isChecked,
            hasAttachment = binding.switchReceipt.isChecked,
            note = enteredNote
        )
        saveTransaction(transaction)
    }

    private fun saveTransaction(transaction: Transaction) {
        val repository = TransactionRepositoryImpl(
            AppDatabase.getInstance(requireContext()).transactionDao()
        )

        viewLifecycleOwner.lifecycleScope.launch {
            repository.addTransaction(transaction)
            findNavController().popBackStack(R.id.homeScreen, false)
        }
    }

    private fun shakeView(view: View) {
        val shake =
            android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        view.startAnimation(shake)
    }

    private fun setupNoteInput() {
        binding.rowNote.setOnClickListener {
            showNoteDialog()
        }
    }

    private fun showNoteDialog() {

        val dialogBinding = DialogAddNoteBinding.inflate(layoutInflater)
        dialogBinding.etNote.setText(enteredNote)

        val dialog =
            MaterialAlertDialogBuilder(requireContext(), R.style.RoundedMaterialDialog).setView(
                dialogBinding.root
            )
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setTextColor("#2E7D32".toColorInt())

            val cancelButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
            cancelButton.setTextColor("#9E9E9E".toColorInt())

            saveButton.setOnClickListener {
                enteredNote = dialogBinding.etNote.text.toString().trim()
                updateNoteDisplay()
            }
        }
        dialog.show()
    }

    private fun updateNoteDisplay() {
        if (enteredNote.isEmpty()) {
            binding.tvNoteValue.text = getString(R.string.add_note_optional)
            binding.tvNoteValue.setTextColor("#9E9E9E".toColorInt())
        } else {
            binding.tvNoteValue.text = enteredNote
            binding.tvNoteValue.setTextColor("#1A1A1A".toColorInt())
        }
    }

    private fun setupDateTimePicker() {
        binding.rowDateTime.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }

        val datePicker = DatePickerDialog(
            requireContext(),
            R.style.RoundedDatePickerTheme,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                showTimePicker(calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showTimePicker(calendar: Calendar) {
        val timePicker = TimePickerDialog(
            requireContext(),
            R.style.RoundedDatePickerTheme,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                selectedTimestamp = calendar.timeInMillis
                updateDateTimeDisplay()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePicker.show()
    }

    private fun updateDateTimeDisplay() {
        binding.tvDateValue.text = DateFormatter.formatTransactionDate(selectedTimestamp)
    }

    private fun setupCategoryPicker() {
        binding.rowCategory.setOnClickListener {
            CategoryPickerSheet().show(childFragmentManager, "CategoryPickerSheet")
        }

        childFragmentManager.setFragmentResultListener(
            CategoryPickerSheet.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->

            val category = bundle.getString(CategoryPickerSheet.RESULT_CATEGORY)
                ?: return@setFragmentResultListener
            applyCategorySelection(category)
        }

    }

    private fun applyCategorySelection(category: String) {
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

    private fun setupTypeTabs() {
        binding.tabExpense.setOnClickListener { applyTypeSelection("Expense") }
        binding.tabIncome.setOnClickListener { applyTypeSelection("Income") }
        binding.tabTransfer.setOnClickListener { applyTypeSelection("Transfer") }
    }

    private fun applyTypeSelection(type: String) {

        binding.tabExpense.background = null
        binding.tabIncome.background = null
        binding.tabTransfer.background = null

        binding.tabExpense.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.tabIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.tabTransfer.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))


        val selectedTab = when (type) {
            "Expense" -> binding.tabExpense
            "Income" -> binding.tabIncome
            else -> binding.tabTransfer
        }

        selectedTab.setBackgroundResource(R.drawable.bg_segment_selected)

        val selectedTextColor = when (type) {
            "Expense" -> "#B71C1C"
            "Income" -> "#1B5E20"
            else -> "#0D47A1"
        }

        selectedTab.setTextColor(selectedTextColor.toColorInt())


        binding.tvScreenTitle.text = when (type) {
            "Expense" -> getString(R.string.new_expense)
            "Income" -> getString(R.string.new_income)
            else -> getString(R.string.new_transfer)
        }
    }

    private fun setupMerchantInput() {
        binding.rowMerchant.setOnClickListener {
            showMerchantDialog()
        }
    }

    private fun showMerchantDialog() {
        val dialogBinding = DialogAddMerchantBinding.inflate(layoutInflater)

        val dao = AppDatabase.getInstance(requireContext()).transactionDao()

        viewLifecycleOwner.lifecycleScope.launch {
            val merchants = dao.getDistinctMerchantNames().first()

            val autoCompleteAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                merchants
            )

            dialogBinding.etMerchantName.setAdapter(autoCompleteAdapter)

            // Agar oldin kiritilgan qiymat bo'lsa, uni ko'rsatamiz
            val currentValue = binding.tvMerchantValue.text.toString()
            if (currentValue != getString(R.string.add_merchant)) {
                dialogBinding.etMerchantName.setText(currentValue, false)
            }

            val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(
                requireContext(), R.style.RoundedMaterialDialog
            )
                .setView(dialogBinding.root)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create()

            dialog.setOnShowListener {
                val saveButton =
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                saveButton.setTextColor("#2E7D32".toColorInt())

                val cancelButton =
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                cancelButton.setTextColor("#9E9E9E".toColorInt())

                saveButton.setOnClickListener {
                    val name = dialogBinding.etMerchantName.text.toString().trim()
                    if (name.isEmpty()) {
                        dialogBinding.tilMerchantName.error = "Please enter a merchant name"
                    } else {
                        binding.tvMerchantValue.text = name
                        binding.tvMerchantValue.setTextColor("#1A1A1A".toColorInt())
                        dialog.dismiss()
                    }
                }
            }

            dialog.show()
        }
    }

    private fun setupAmountInput() {

        binding.etAmount.addTextChangedListener(object : TextWatcher {
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

    private fun setupCloseButton() {
        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}