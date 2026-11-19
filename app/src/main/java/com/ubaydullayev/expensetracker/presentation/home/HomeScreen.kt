package com.ubaydullayev.expensetracker.presentation.home

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.adapter.budgetsadapter.BudgetsAdapter
import com.ubaydullayev.expensetracker.adapter.transactionadapter.TransactionsAdapter
import com.ubaydullayev.expensetracker.adapter.upcomingbillsadapter.UpcomingBillsAdapter
import com.ubaydullayev.expensetracker.data.local.database.AppDatabase
import com.ubaydullayev.expensetracker.data.local.prefs.UserPreferences
import com.ubaydullayev.expensetracker.data.repository.BudgetRepositoryImpl
import com.ubaydullayev.expensetracker.data.repository.SavingGoalRepositoryImpl
import com.ubaydullayev.expensetracker.data.repository.TransactionRepositoryImpl
import com.ubaydullayev.expensetracker.data.repository.UpcomingBillRepositoryImpl
import com.ubaydullayev.expensetracker.databinding.ScreenHomeBinding
import com.ubaydullayev.expensetracker.domain.model.SavingGoal
import com.ubaydullayev.expensetracker.presentation.common.BaseViewModelFragment
import com.ubaydullayev.expensetracker.presentation.common.applySystemBarInsetsAsPadding
import com.ubaydullayev.expensetracker.utils.HorizontalSpaceDecoration
import com.ubaydullayev.expensetracker.utils.formatAmount
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeScreen :
    BaseViewModelFragment<ScreenHomeBinding, HomeViewModel>(HomeViewModel::class.java) {

    // The bottom bar now lives in the activity shell (below this fragment) and consumes the
    // navigation-bar inset itself, so Home must not add that inset again or a gap appears above the bar.
    override val insetBottom: Boolean = false

    // Yashil hero status bar ostiga ham cho'ziladi: root'ga tepa inset qo'shilmaydi,
    // u faqat heroContainer'ning paddingTop'iga qo'shiladi (setupHeroInsets).
    // Fon to'q yashil bo'lgani uchun status bar ikonkalari oq.
    override val insetTop: Boolean = false
    override val lightStatusBars: Boolean = false

    override val viewModelFactory: ViewModelProvider.Factory
        get() {
            val database = AppDatabase.getInstance(requireContext())
            return HomeViewModelFactory(
                transactionRepository = TransactionRepositoryImpl(database.transactionDao()),
                upcomingBillRepository = UpcomingBillRepositoryImpl(database.upcomingBillDao()),
                savingGoalRepository = SavingGoalRepositoryImpl(database.savingGoalDao()),
                budgetRepository = BudgetRepositoryImpl(database.budgetDao())
            )
        }

    private lateinit var transactionsAdapter: TransactionsAdapter
    private lateinit var upcomingBillsAdapter: UpcomingBillsAdapter
    private lateinit var budgetsAdapter: BudgetsAdapter


    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenHomeBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeroInsets()
        binding.tvGreeting.setText(greetingRes())
        setupRecyclerViews()
        observeData()
        setupClickListeners()
    }

    private fun setupRecyclerViews() {
        transactionsAdapter = TransactionsAdapter(emptyList())
        binding.rvRecentTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionsAdapter
            isNestedScrollingEnabled = false
        }

        upcomingBillsAdapter = UpcomingBillsAdapter(emptyList())
        binding.rvUpcomingBills.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = upcomingBillsAdapter
            isNestedScrollingEnabled = false
        }

        budgetsAdapter = BudgetsAdapter(emptyList())
        binding.budgetRecycler.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = budgetsAdapter
            isNestedScrollingEnabled = false
            addItemDecoration(HorizontalSpaceDecoration(12))
        }

        setupGoalCardClickListener()
    }


    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle((Lifecycle.State.STARTED)) {
                launch {
                    viewModel.recentTransactions.collect { transactions ->
                        binding.sectionRecentTransactions.visibility =
                            if (transactions.isEmpty()) View.GONE else View.VISIBLE
                        transactionsAdapter.updateData(transactions)
                    }
                }

                launch {
                    viewModel.upcomingBill.collect { upcomingBills ->
                        binding.sectionUpcomingBills.visibility =
                            if (upcomingBills.isEmpty()) View.GONE else View.VISIBLE
                        upcomingBillsAdapter.updateData(upcomingBills)
                    }
                }
                launch {
                    viewModel.savingGoals.collect { savingGoals ->
                        val topGoal = savingGoals.firstOrNull()
                        binding.sectionSavingGoals.visibility =
                            if (topGoal == null) View.GONE else View.VISIBLE
                        if (topGoal != null) bindGoalCard(topGoal)
                    }
                }
                launch {
                    viewModel.budgets.collect { budgets ->
                        binding.sectionBudgets.visibility =
                            if (budgets.isEmpty()) View.GONE else View.VISIBLE
                        budgetsAdapter.updateData(budgets)
                    }
                }
                launch {
                    viewModel.balanceSummary.collect { summary ->
                        bindBalanceCard(summary)
                    }
                }
                launch {
                    viewModel.financialHealth.collect { health ->
                        bindFinancialHealth(health)
                    }
                }
                // Ism, initsiallar va valyuta Profile ekranidagi bilan bir manbadan
                launch {
                    UserPreferences.getInstance(requireContext()).profile.collect { profile ->
                        binding.tvUserName.text = profile.name
                        binding.ivProfile.text = profile.initials
                    }
                }
                launch {
                    UserPreferences.getInstance(requireContext()).currencyCode.collect { code ->
                        binding.btnCurrency.text = code
                    }
                }
            }
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun bindBalanceCard(summary: BalanceSummary) {
        binding.tvTotalBalance.text = formatAmount(summary.totalBalance)
        binding.tvIncome.text = formatAmount(summary.monthlyIncome)
        binding.tvExpense.text = formatAmount(summary.monthlyExpense)

        val trend = summary.trendPercent
        if (trend == null || trend.isNaN() || trend.isInfinite()) {
            // O'tgan oyda ma'lumot yo'q — "-100%" o'rniga badge'ni yashiramiz.
            // GONE: yangi hero'da badge'lar markazda, yonidagi "Saving" yorlig'i bo'sh joy qoldirmasin.
            binding.trendBadge.visibility = View.GONE
        } else {
            binding.trendBadge.visibility = View.VISIBLE
            val isUp = trend >= 0
            binding.ivTrendArrow.setImageResource(if (isUp) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down)
            binding.ivTrendArrow.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    if (isUp) R.color.income_green else R.color.expense_red
                )
            )
            val sign = if (isUp) "+" else ""
            binding.tvMonthlyChange.text = "$sign${String.format("%.1f", trend)}% this month"
        }
    }

    // Eski Financial Health kartasi olib tashlandi: score aslida tejash foizi,
    // shuning uchun u hero'dagi "Saving 24%" yorlig'ida ko'rsatiladi.
    private fun bindFinancialHealth(health: FinancialHealth) {
        val hasData = health.label != "No Data"
        binding.savingBadge.visibility = if (hasData) View.VISIBLE else View.GONE
        binding.tvSavingRate.text = getString(R.string.home_saving_rate, health.score)
    }

    private fun setupHeroInsets() {
        binding.heroContainer.applySystemBarInsetsAsPadding(
            applyLeft = false,
            applyTop = true,
            applyRight = false,
            applyBottom = false,
        )
    }

    private fun greetingRes(): Int =
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> R.string.good_morning
            in 12..17 -> R.string.good_afternoon
            else -> R.string.good_evening
        }

    @SuppressLint("SetTextI18n")
    private fun bindGoalCard(goal: SavingGoal) {
        val cardBinding = binding.includeGoalCard
        cardBinding.tvGoalName.text = goal.title
        cardBinding.tvCategoryTag.text = goal.category
        cardBinding.tvEstDate.text = goal.estimatedDate
        cardBinding.tvPriority.text = goal.priority
        cardBinding.tvTargetDate.text = goal.targetDate

        val percent = ((goal.currentAmount / goal.targetAmount) * 100).toInt()
        cardBinding.tvPercent.text = "$percent%"
        cardBinding.goalProgressRing.progress = percent

        cardBinding.tvSaved.text = formatAmount(goal.currentAmount)
        cardBinding.tvTargetAmount.text = "/ ${formatAmount(goal.targetAmount)}"

        val remaining = goal.targetAmount - goal.currentAmount
        cardBinding.tvToGo.text = "${formatAmount(remaining)} to go"

        val fillParams = cardBinding.goalProgressFill.layoutParams as ConstraintLayout.LayoutParams
        fillParams.matchConstraintPercentWidth = percent / 100f
        cardBinding.goalProgressFill.layoutParams = fillParams
    }


    private fun setupGoalCardClickListener() {
        // Kartaning o'ziga bosilganda -> to'liq Saving Goals screen'iga o'tish.
        // "See all" tugmasi listener'i setupClickListeners() ichida bir joyda beriladi.
        binding.includeGoalCard.root.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_savingGoalsScreen)
        }
    }

    private fun setupClickListeners() {

        // Recent Transactions "See all"
        binding.root.findViewById<View>(R.id.tvSeeAllTransactions)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_transactionScreen)
        }
        // Budgets "See all"
        binding.btnSeeAllBudgets.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_budgetScreen)
        }

        // Profile avatar
        binding.ivProfile.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_profileScreen)
        }
        // Valyuta Profile ekranida o'zgartiriladi
        binding.btnCurrency.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_profileScreen)
        }
        // Quick Action — Add to goal (Add endi bottom bar'dagi "+" da)
        binding.btnAddToGoal.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_savingGoalsScreen)
        }

        // Upcoming Bills uchun alohida ekran hali yo'q: bosilsa hech narsa
        // bo'lmaydigan "See all" ko'rsatmaymiz. Ekran qo'shilgach shu yerda ulang.
        binding.btnSeeUpcomingBills.visibility = View.GONE

        // SavingGoals "See all"
        binding.btnSeeAllSavingGoals.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_savingGoalsScreen)
        }

        binding.btnNotification.setOnClickListener {
            findNavController().navigate(R.id.notificationsScreen)
        }
    }

}