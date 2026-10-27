package com.ubaydullayev.expensetracker.presentation.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.adapter.budgetsadapter.CategoryAdapter
import com.ubaydullayev.expensetracker.databinding.ScreenBudgetBinding

class BudgetScreen : Fragment() {

    private var _binding: ScreenBudgetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ScreenBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOverview()
        setupCategories()
        setupHistory()

        binding.btnBudget.setOnClickListener {
            // TODO: yangi budget qo'shish ekraniga o'tish
        }
        binding.btnEditCategories.setOnClickListener {
            // TODO: kategoriyalarni tahrirlash
        }
    }

    private fun setupOverview() = with(binding) {
        progressSpent.setProgressCompat(68, true)
        tvSpentAmount.text = "$2,650"
        tvTotalAmount.text = "of $3,900"
        tvRemaining.text = "$1,250"
        tvDailyLeft.text = "$96"
        chipStatus.text = "On track · 68% used"
        tvSmartTip.text = "Reduce Dining by $80 to stay under budget this month."
    }

    private fun setupCategories() {
        val items = listOf(
            BudgetCategory(
                "Housing", "Est. $1,420 by month end", 900, 1500, 60, "$600 left",
                BudgetStatus.SAFE, false,
                R.drawable.ic_home, 0xFFDDF5E8.toInt(), 0xFF0F7B55.toInt()
            ),
            BudgetCategory(
                "Groceries", "Est. $640 by month end", 530, 600, 88, "$70 left",
                BudgetStatus.WARNING, true,
                R.drawable.ic_cart, 0xFFDCEAFE.toInt(), 0xFF2563EB.toInt()
            ),
            BudgetCategory(
                "Dining", "Est. $520 by month end", 450, 400, 112, "$50 over",
                BudgetStatus.OVERSPENT, true,
                R.drawable.ic_restaurant, 0xFFFEE4E2.toInt(), 0xFFD92D20.toInt()
            ),
            BudgetCategory(
                "Transport", "Budget complete", 300, 300, 100, "Fully used",
                BudgetStatus.COMPLETED, false,
                R.drawable.ic_car, 0xFFEEF0F3.toInt(), 0xFF4B5563.toInt()
            )
        )
        binding.rvCategories.adapter = CategoryAdapter(items)
    }

    private fun setupHistory() {
        val h = binding.budgetHistory           // include'ning binding'i
        val data = listOf(
            h.barOct to 3100, h.barNov to 3600, h.barDec to 4300,
            h.barJan to 2800, h.barFeb to 3400, h.barMar to 3240
        )
        val maxBarDp = 92                        // eng baland ustun balandligi (dp)
        val maxValue = data.maxOf { it.second }
        val density = resources.displayMetrics.density

        data.forEach { (bar, value) ->
            bar.layoutParams = bar.layoutParams.apply {
                height = ((value.toFloat() / maxValue) * maxBarDp * density).toInt()
            }
        }
        h.tvAvgSpend.text = "$3,240"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // xotira oqishining oldini oladi
    }
}