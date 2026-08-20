package com.ubaydullayev.expensetracker.presentation.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.adapter.transactionadapter.Transaction
import com.ubaydullayev.expensetracker.adapter.transactionadapter.TransactionsAdapter
import com.ubaydullayev.expensetracker.adapter.upcomingbillsadapter.UpcomingBill
import com.ubaydullayev.expensetracker.adapter.upcomingbillsadapter.UpcomingBillsAdapter
import com.ubaydullayev.expensetracker.databinding.ScreenHomeBinding
import com.ubaydullayev.expensetracker.presentation.common.BaseViewModelFragment

class HomeScreen : BaseViewModelFragment<ScreenHomeBinding, HomeViewModel>(HomeViewModel::class.java) {

    // The bottom bar now lives in the activity shell (below this fragment) and consumes the
    // navigation-bar inset itself, so Home must not add that inset again or a gap appears above the bar.
    override val insetBottom: Boolean = false

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenHomeBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUpcomingBills()
        setupRecentTransactions()
    }

    private fun setupRecentTransactions() {
        binding.rvRecentTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = TransactionsAdapter(recentTransactions())
            isNestedScrollingEnabled = false
        }
    }

    /** Sample recent transactions shown on Home; the full history opens via "See all". */
    private fun recentTransactions(): List<Transaction> = listOf(
        Transaction(
            id = "1",
            title = "Grocery Store",
            category = "Food",
            categoryColor = Color.parseColor("#E65100"),
            categoryBgColor = Color.parseColor("#FFF3E0"),
            dateTime = "Today, 2:30 PM",
            amount = 54.20,
            isIncome = false,
            iconRes = R.drawable.ic_scan,
            iconBgColor = Color.parseColor("#FFF3E0"),
        ),
        Transaction(
            id = "2",
            title = "Salary",
            category = "Income",
            categoryColor = Color.parseColor("#2E7D32"),
            categoryBgColor = Color.parseColor("#E8F5E9"),
            dateTime = "Today, 9:00 AM",
            amount = 3200.00,
            isIncome = true,
            iconRes = R.drawable.ic_stats,
            iconBgColor = Color.parseColor("#E8F5E9"),
        ),
        Transaction(
            id = "3",
            title = "Netflix Premium",
            category = "Entertainment",
            categoryColor = Color.parseColor("#C2185B"),
            categoryBgColor = Color.parseColor("#FCE4EC"),
            dateTime = "Yesterday, 6:15 PM",
            amount = 22.99,
            isIncome = false,
            iconRes = R.drawable.ic_play,
            iconBgColor = Color.parseColor("#FCE4EC"),
        ),
        Transaction(
            id = "4",
            title = "Flight Ticket",
            category = "Travel",
            categoryColor = Color.parseColor("#1565C0"),
            categoryBgColor = Color.parseColor("#E3F2FD"),
            dateTime = "Yesterday, 11:40 AM",
            amount = 189.00,
            isIncome = false,
            iconRes = R.drawable.ic_plane,
            iconBgColor = Color.parseColor("#E3F2FD"),
        ),
    )

    private fun setupUpcomingBills() {
        // Show only the 2-3 nearest bills here; the full list opens via "See all".
        val nearestBills = upcomingBills()
            .sortedBy { it.dueInDays }
            .take(3)
            .map { it.bill }

        binding.rvUpcomingBills.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = UpcomingBillsAdapter(nearestBills)
            isNestedScrollingEnabled = false
        }
    }

    /** Sample bills paired with a due-in-days value used only for sorting. */
    private fun upcomingBills(): List<BillEntry> = listOf(
        BillEntry(
            dueInDays = 2,
            bill = UpcomingBill(
                id = "netflix",
                title = "Netflix Premium",
                category = "Entertainment",
                frequency = "Monthly",
                price = 22.99,
                dueText = "Due in 2d",
                isUrgent = true,
                iconRes = R.drawable.ic_play,
                iconBgColor = Color.parseColor("#E53935"),
            ),
        ),
        BillEntry(
            dueInDays = 5,
            bill = UpcomingBill(
                id = "car_insurance",
                title = "Car Insurance",
                category = "Insurance",
                frequency = "Monthly",
                price = 140.00,
                dueText = "Due in 5d",
                isUrgent = false,
                iconRes = R.drawable.ic_shield,
                iconBgColor = Color.parseColor("#F59E0B"),
            ),
        ),
        BillEntry(
            dueInDays = 9,
            bill = UpcomingBill(
                id = "adobe",
                title = "Adobe Creative",
                category = "Software",
                frequency = "Monthly",
                price = 54.99,
                dueText = "Due in 9d",
                isUrgent = false,
                iconRes = R.drawable.ic_laptop,
                iconBgColor = Color.parseColor("#2E7D32"),
            ),
        ),
    )

    private data class BillEntry(val dueInDays: Int, val bill: UpcomingBill)
}
