package com.ubaydullayev.expensetracker.presentation.addTransaction.categorysheet

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.adapter.listadapter.CategoryPickerAdapter
import com.ubaydullayev.expensetracker.data.local.database.AppDatabase
import com.ubaydullayev.expensetracker.data.local.entity.CustomCategoryEntity
import com.ubaydullayev.expensetracker.databinding.DialogAddCategoryBinding
import com.ubaydullayev.expensetracker.databinding.DialogCategoryAddedBinding
import com.ubaydullayev.expensetracker.databinding.SheetCategoryPickerBinding
import com.ubaydullayev.expensetracker.utils.TransactionCategory
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt
import com.google.android.material.textfield.TextInputLayout

class CategoryPickerSheet : BottomSheetDialogFragment() {

    companion object {
        const val REQUEST_KEY = "category_picker_request"
        const val RESULT_CATEGORY = "selected_category"
    }

    private var _binding: SheetCategoryPickerBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CategoryPickerAdapter

    private var successDialog: Dialog? = null
    private val dismissHandler = Handler(Looper.getMainLooper())
    private var dismissRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetCategoryPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CategoryPickerAdapter(TransactionCategory.ALL) { selectedCategory ->
            setFragmentResult(REQUEST_KEY, Bundle().apply {
                putString(RESULT_CATEGORY, selectedCategory)
            })
            dismiss()
        }

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CategoryPickerSheet.adapter
        }

        binding.btnAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        observeCustomCategories()
    }

    private fun observeCustomCategories() {
        val dap = AppDatabase.getInstance(requireContext()).customCategoryDao()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dap.getAllCustomCategories().collect { customCategories ->
                    val allNames = customCategories.map { it.name } + TransactionCategory.ALL
                    adapter.updateCategories(allNames)
                }
            }
        }
    }

    private fun showAddCategoryDialog() {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.RoundedMaterialDialog)
            .setView(dialogBinding.root)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.setTextColor("#2E7D32".toColorInt())

            val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            cancelButton.setTextColor("#9E9E9E".toColorInt())

            addButton.setOnClickListener {
                val name = dialogBinding.etNewCategoryName.text.toString().trim()

                when {
                    name.isEmpty() -> {
                        shakeAndError(dialogBinding.tilCategoryName, "Please enter a category name")
                    }

                    else -> checkAndSaveCategory(name, dialogBinding, dialog)
                }
            }
        }
        dialog.show()
    }

    private fun checkAndSaveCategory(
        name: String,
        dialogBinding: DialogAddCategoryBinding,
        dialog: Dialog
    ) {
        val dao = AppDatabase.getInstance(requireContext()).customCategoryDao()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val existsInDefaults =
                    TransactionCategory.ALL.any { it.equals(name, ignoreCase = true) }
                val existsInCustom = dao.categoryExists(name)

                if (existsInDefaults || existsInCustom) {
                    shakeAndError(dialogBinding.tilCategoryName, "This category already exists")
                    return@launch
                }

                dao.insert(CustomCategoryEntity(name = name))
                dialog.dismiss()
                showSuccessDialog()

            } catch (e: android.database.sqlite.SQLiteConstraintException) {
                shakeAndError(dialogBinding.tilCategoryName, "This category already exists")
            }
        }
    }

    private fun showSuccessDialog() {
        val successBinding = DialogCategoryAddedBinding.inflate(layoutInflater)

        // MUHIM: "val" emas — klass darajasidagi "successDialog"ga TO'G'RIDAN-TO'G'RI yozamiz
        successDialog = Dialog(requireContext())
        successDialog?.setContentView(successBinding.root)
        successDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        successDialog?.setCancelable(false)
        successDialog?.show()

        // Lottie animatsiyasiga tayanmasdan, vaqt bo'yicha ISHONCHLI yopish
        dismissRunnable = Runnable { dismissSuccessDialogSafely() }
        dismissHandler.postDelayed(dismissRunnable!!, 2400)
    }

    private fun dismissSuccessDialogSafely() {
        dismissRunnable?.let { dismissHandler.removeCallbacks(it) }
        dismissRunnable = null

        if (isAdded && successDialog?.isShowing == true) {
            try {
                successDialog?.dismiss()
            } catch (e: IllegalArgumentException) {
                // Oyna allaqachon yo'q bo'lib qolgan holatlar uchun xavfsizlik
            }
        }
        successDialog = null
    }

    private fun shakeAndError(inputLayout: TextInputLayout, message: String) {
        inputLayout.error = message
        val shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        inputLayout.startAnimation(shake)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissSuccessDialogSafely()
        _binding = null
    }
}