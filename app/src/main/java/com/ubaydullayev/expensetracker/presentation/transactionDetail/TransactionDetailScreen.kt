package com.ubaydullayev.expensetracker.presentation.transactionDetail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.data.local.database.AppDatabase
import com.ubaydullayev.expensetracker.databinding.ScreenTransactionDetailBinding
import com.ubaydullayev.expensetracker.domain.model.Transaction
import com.ubaydullayev.expensetracker.presentation.common.BaseFragment
import com.ubaydullayev.expensetracker.utils.CategoryStyleProvider
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import com.ubaydullayev.expensetracker.data.local.mapper.toDomainModel
import com.ubaydullayev.expensetracker.data.local.mapper.toEntity


class TransactionDetailScreen : BaseFragment<ScreenTransactionDetailBinding>() {

    private val args: TransactionDetailScreenArgs by navArgs()
    private var currentTransaction: Transaction? = null

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenTransactionDetailBinding.inflate(inflater, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadTransaction()
        setupActions()
    }

    private fun loadTransaction() {
        val dao = AppDatabase.getInstance(requireContext()).transactionDao()

        viewLifecycleOwner.lifecycleScope.launch {
            val entity = dao.getTransactionById(args.transactionId)
            if (entity == null) {
                findNavController().popBackStack()
                return@launch
            }

            // ENDI HAMMASI "launch" BLOKI ICHIDA
            val transaction = entity.toDomainModel()
            currentTransaction = transaction
            bindTransaction(transaction)
        }
    }


    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun bindTransaction(transaction: Transaction) {
        val style = CategoryStyleProvider.getStyle(transaction.category)


        binding.ivHeroIcon.setImageResource(style.iconRes)
        binding.tvHeroMerchant.text = transaction.title
        binding.tvHeroCategory.text = transaction.category

        val sign = if (transaction.isIncome) "+" else "−"
        binding.tvHeroAmount.text = "$sign$${String.format("%,.2f", transaction.amount)}"

        // Details
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        binding.tvDetailDate.text = dateFormat.format(transaction.timestamp)
        binding.tvDetailTime.text = timeFormat.format(transaction.timestamp)

        if (transaction.note.isBlank()) {
            binding.notesCard.visibility = View.GONE
        } else {
            binding.notesCard.visibility = View.VISIBLE
            binding.tvNoteContent.text = transaction.note
        }

        // Recurring badge — faqat isRecurring=true bo'lsa ko'rinadi
        binding.recurringCard.visibility =
            if (transaction.isRecurring) View.VISIBLE else View.GONE
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.btnEdit.setOnClickListener {
            // TODO: Edit funksiyasi — AddTransactionScreen hozircha faqat "yangi qo'shish"
            // rejimida ishlaydi, tahrirlash rejimi keyinroq qo'shiladi
        }

        binding.btnDuplicate.setOnClickListener {
            // TODO: keyinroq
        }

        binding.btnHeroShare.setOnClickListener {
            // TODO: keyinroq
        }
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext(), R.style.RoundedMaterialDialog)
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteTransaction()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTransaction() {
        val transaction = currentTransaction ?: return
        val dao = AppDatabase.getInstance(requireContext()).transactionDao()

        viewLifecycleOwner.lifecycleScope.launch {
            dao.delete(transaction.toEntity())
            findNavController().popBackStack()
        }
    }
}