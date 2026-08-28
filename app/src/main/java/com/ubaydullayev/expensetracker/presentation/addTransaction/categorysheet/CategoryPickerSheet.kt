package com.ubaydullayev.expensetracker.presentation.addTransaction.categorysheet

import android.R.attr.name
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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
import com.ubaydullayev.expensetracker.databinding.SheetCategoryPickerBinding
import com.ubaydullayev.expensetracker.utils.TransactionCategory
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DividerItemDecoration

class CategoryPickerSheet : BottomSheetDialogFragment() {


    companion object {
        const val REQUEST_KEY = "category_picker_request"
        const val RESULT_CATEGORY = "selected_category"
    }

    private var _binding: SheetCategoryPickerBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CategoryPickerAdapter

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

        // 1. AVVAL klass darajasidagi "adapter"ni to'ldiramiz
        adapter = CategoryPickerAdapter(TransactionCategory.ALL) { selectedCategory ->
            setFragmentResult(REQUEST_KEY, Bundle().apply {
                putString(RESULT_CATEGORY, selectedCategory)
            })
            dismiss()
        }

        // 2. Keyin RecyclerView'ga ulaymiz
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CategoryPickerSheet.adapter

            val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            divider.setDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.divider_thin)!!
            )
            addItemDecoration(divider)

        }

        binding.btnAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        // 3. Eng oxirida kuzatishni boshlaymiz (chunki bu "adapter"ni ishlatadi)
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

        val dialog = MaterialAlertDialogBuilder(
            requireContext(),
            R.style.RoundedMaterialDialog
        )
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
                if (name.isNotEmpty()) {
                    saveNewCategory(name)
                    dialog.dismiss()
                } else {
                    dialogBinding.tilCategoryName.error = "Please enter a category name"
                }
            }
        }
        dialog.show()
    }

    private fun saveNewCategory(name: String) {
        val dao = AppDatabase.getInstance(requireContext()).customCategoryDao()
        viewLifecycleOwner.lifecycleScope.launch {
            dao.insert(CustomCategoryEntity(name = name))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}