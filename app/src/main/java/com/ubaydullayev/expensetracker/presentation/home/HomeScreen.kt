package com.ubaydullayev.expensetracker.presentation.home

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Outline
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
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

    // Ko'z tugmasi: summalarni yashirish. Oxirgi summary saqlanadi, toggle'da qayta chiziladi.
    private var isBalanceHidden = false
    private var lastSummary: BalanceSummary? = null

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenHomeBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isBalanceHidden = savedInstanceState?.getBoolean(KEY_BALANCE_HIDDEN) ?: isBalanceHidden
        setupHeroInsets()
        updateBalanceToggleIcon()
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
                launch {
                    viewModel.safeToSpend.collect { bindSafeToSpend(it) }
                }
                launch {
                    viewModel.insight.collect { bindInsight(it) }
                }
                // Ism, initsiallar va valyuta Profile ekranidagi bilan bir manbadan
                launch {
                    val prefs = UserPreferences.getInstance(requireContext())
                    prefs.profile.collect { profile ->
                        // Ro'yxatdan o'tishda ism faqat Firebase'ga yozilgan bo'lishi mumkin:
                        // prefs'da hali standart qiymat bo'lsa, Firebase'dagisini ko'chiramiz.
                        val firebaseUser = Firebase.auth.currentUser
                        val firebaseName = firebaseUser?.displayName.orEmpty().trim()
                        if (profile.name == DEFAULT_PROFILE_NAME && firebaseName.isNotEmpty()) {
                            prefs.updateProfile(firebaseName, firebaseUser?.email.orEmpty())
                            return@collect // yangilangan profil shu collect'ga qayta keladi
                        }
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
        lastSummary = summary
        binding.tvTotalBalance.text = maskedOr(formatAmount(summary.totalBalance))
        binding.tvIncome.text = maskedOr(formatAmount(summary.monthlyIncome))
        binding.tvExpense.text = maskedOr(formatAmount(summary.monthlyExpense))

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

    private fun maskedOr(amount: String): String = if (isBalanceHidden) BALANCE_MASK else amount

    private fun toggleBalanceVisibility() {
        isBalanceHidden = !isBalanceHidden
        updateBalanceToggleIcon()
        lastSummary?.let { bindBalanceCard(it) }
    }

    private fun updateBalanceToggleIcon() {
        binding.btnToggleBalance.setIconResource(
            if (isBalanceHidden) R.drawable.ic_eye_off else R.drawable.ic_eye
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_BALANCE_HIDDEN, isBalanceHidden)
    }

    // Eski Financial Health kartasi olib tashlandi: score aslida tejash foizi,
    // shuning uchun u hero'dagi "Saving 24%" yorlig'ida ko'rsatiladi.
    private fun bindFinancialHealth(health: FinancialHealth) {
        val hasData = health.label != "No Data"
        binding.savingBadge.visibility = if (hasData) View.VISIBLE else View.GONE
        binding.tvSavingRate.text = getString(R.string.home_saving_rate, health.score)
    }

    private fun bindSafeToSpend(safe: SafeToSpend?) {
        binding.cardSafeToSpend.visibility = if (safe == null) View.GONE else View.VISIBLE
        if (safe == null) return

        binding.tvSafeToSpend.text = getString(R.string.home_per_day, formatAmount(safe.perDay))
        binding.tvSafeStatus.setText(if (safe.isOverLimit) R.string.home_limit_reached else R.string.home_on_track)
        // Holat rangi: limit oshgan bo'lsa qizil, aks holda yashil (finara_colors.xml)
        binding.tvSafeStatus.setTextColor(
            ContextCompat.getColor(requireContext(), if (safe.isOverLimit) R.color.fin_error else R.color.fin_success)
        )
        binding.tvSafeStatus.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), if (safe.isOverLimit) R.color.fin_error_tint else R.color.fin_success_tint)
        )
        binding.progressMonthlyBudget.setProgressCompat(safe.progressPercent, false)
        binding.tvSafeToSpendMeta.text = getString(
            if (safe.basedOnBudget) R.string.home_safe_meta_budget else R.string.home_safe_meta_income,
            formatAmount(safe.spent),
            formatAmount(safe.limit),
            resources.getQuantityString(R.plurals.home_days_left, safe.daysLeft, safe.daysLeft)
        )
    }

    private fun bindInsight(insight: SpendingInsight?) {
        binding.cardInsight.visibility = if (insight == null) View.GONE else View.VISIBLE
        if (insight == null) return
        binding.tvInsightText.text = insight.text
        binding.tvInsightPeriod.text = insight.period
        binding.btnInsightAction.setText(R.string.home_see_transactions)
    }

    private fun setupHeroInsets() {
        // Girih naqshi hero'ning pastki yumaloq burchaklaridan chiqmasligi uchun kesamiz.
        // Tepasi ekran chetida, shuning uchun outline yuqoriga cho'zilgan: faqat pastki
        // burchaklar yumaloq bo'ladi (roundRect barcha Android versiyalarida kesa oladi).
        val radius = 30f * resources.displayMetrics.density
        binding.heroContainer.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, -radius.toInt(), view.width, view.height, radius)
            }
        }
        binding.heroContainer.clipToOutline = true

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

        // Balansni yashirish / ko'rsatish
        binding.btnToggleBalance.setOnClickListener { toggleBalanceVisibility() }

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

        // Insight -> tranzaksiyalar ro'yxati
        binding.btnInsightAction.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_transactionScreen)
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

    private companion object {
        /** UserPreferences'dagi standart ism (foydalanuvchi hali ism kiritmagan). */
        const val DEFAULT_PROFILE_NAME = "Your Name"
        const val KEY_BALANCE_HIDDEN = "home_balance_hidden"
        const val BALANCE_MASK = "••••••"
    }
}