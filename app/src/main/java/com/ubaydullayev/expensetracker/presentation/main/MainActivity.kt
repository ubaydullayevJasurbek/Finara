package com.ubaydullayev.expensetracker.presentation.main

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    /** Indikator hali joylashtirilmagan bo'lsa, birinchi marta animatsiyasiz qo'yiladi. */
    private var indicatorPlaced = false

    /** Bitta tab: bosiladigan konteyner, ikonka, yozuv va u ochadigan ekran. */
    private class Tab(
        val root: View,
        val icon: ImageView,
        val label: TextView,
        val destinationId: Int,
    )

    private val tabs: List<Tab> by lazy {
        listOf(
            Tab(binding.tabHome, binding.iconHome, binding.labelHome, R.id.homeScreen),
            Tab(
                binding.tabTransactions,
                binding.iconTransactions,
                binding.labelTransactions,
                R.id.transactionScreen
            ),
            Tab(binding.tabBudget, binding.iconBudget, binding.labelBudget, R.id.budgetScreen),
            Tab(binding.tabProfile, binding.iconProfile, binding.labelProfile, R.id.profileScreen),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = (supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

        applyBottomBarInset()
        setupBottomBar()
        reserveSpaceUnderBottomBar()
    }

    private fun setupBottomBar() {
        tabs.forEach { tab ->
            tab.root.setOnClickListener { navigateToTab(tab.destinationId) }
        }
        binding.btnNavAdd.setOnClickListener { openAddFlow() }

        // Bar faqat tab ekranlarida ko'rinadi; faol tab ekranga qarab belgilanadi
        // (orqaga qaytganda ham, boshqa joydan navigate qilinganda ham to'g'ri qoladi).
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTabScreen = tabs.any { it.destinationId == destination.id }
            binding.bottomBarContainer.isVisible = isTabScreen
            if (isTabScreen) selectTab(destination.id)
        }
    }

    private fun selectTab(destinationId: Int) {
        val activeColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.accent_dark))
        val inactiveColor =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.nav_inactive))
        val medium = ResourcesCompat.getFont(this, R.font.inter_medium)

        tabs.forEach { tab ->
            val active = tab.destinationId == destinationId
            tab.root.isSelected = active
            tab.icon.imageTintList = if (active) activeColor else inactiveColor
            tab.label.setTextColor(if (active) activeColor else inactiveColor)
            tab.label.setTypeface(medium, if (active) Typeface.BOLD else Typeface.NORMAL)
        }

        tabs.firstOrNull { it.destinationId == destinationId }?.let { moveIndicatorTo(it) }
    }

    /**
     * Pill'ni tanlangan tab ostiga suzdirib olib o'tadi. Tablar bir xil kenglikda
     * (layout_weight=1), shuning uchun faqat translationX animatsiya qilinadi.
     */
    private fun moveIndicatorTo(tab: Tab) {
        val indicator = binding.navIndicator
        tab.root.doOnLayout {
            // Kenglik tab kengligiga teng (ekran aylanganda ham yangilanadi)
            if (indicator.width != tab.root.width) {
                indicator.updateLayoutParams { width = tab.root.width }
            }
            val targetX = tab.root.left.toFloat()

            if (!indicatorPlaced) {
                indicator.translationX = targetX
                indicator.visibility = View.VISIBLE
                indicatorPlaced = true
            } else {
                indicator.animate()
                    .translationX(targetX)
                    .setDuration(INDICATOR_ANIM_MS)
                    .setInterpolator(OvershootInterpolator(0.8f))
                    .start()
            }
        }
    }

    private fun navigateToTab(destinationId: Int) {
        if (navController.currentDestination?.id == destinationId) return
        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .setPopUpTo(R.id.homeScreen, /* inclusive = */ false, /* saveState = */ true)
            .build()
        navController.navigate(destinationId, null, options)
    }

    private fun openAddFlow() {
        navController.navigate(R.id.addFabScreen)
    }

    /**
     * Bar kontent ustida suzadi, shuning uchun har bir ekranning skroll qilinadigan view'iga
     * bar balandligicha pastki padding qo'shamiz (clipToPadding=false): kontent bar ostidan
     * o'tadi, lekin oxirgi element bar tagida qolib ketmaydi. Har bir ekranni alohida
     * o'zgartirish shart emas.
     */
    private fun reserveSpaceUnderBottomBar() {
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHost.childFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentViewCreated(
                    fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?
                ) {
                    val target = findScrollingChild(v) ?: v
                    val basePadding = target.paddingBottom
                    if (target is ViewGroup) target.clipToPadding = false

                    // Bar o'lchami (va navigation-bar inset'i) o'zgarganda ham yangilanadi
                    val listener = View.OnLayoutChangeListener { bar, _, _, _, _, _, _, _, _ ->
                        val reserved = if (bar.isVisible) bar.height else 0
                        if (target.paddingBottom != basePadding + reserved) {
                            target.updatePadding(bottom = basePadding + reserved)
                        }
                    }
                    binding.bottomBarContainer.addOnLayoutChangeListener(listener)
                    binding.bottomBarContainer.requestLayout()

                    f.viewLifecycleOwnerLiveData.observe(f) { owner ->
                        if (owner == null) binding.bottomBarContainer.removeOnLayoutChangeListener(
                            listener
                        )
                    }
                }
            },
            false
        )
    }

    /** Ekran ichidagi birinchi skroll view'ni topadi (NestedScrollView yoki RecyclerView). */
    private fun findScrollingChild(view: View): View? {
        if (view is NestedScrollView || view is RecyclerView || view is android.widget.ScrollView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findScrollingChild(view.getChildAt(i))?.let { return it }
            }
        }
        return null
    }

    private companion object {
        const val INDICATOR_ANIM_MS = 320L
    }

    private fun applyBottomBarInset() {
        // XML'dagi 12dp pastki bo'shliqni saqlab, ustiga navigation-bar balandligini qo'shamiz
        // (aks holda inset uni almashtirib yuborib, bar ekranning pastiga yopishib qoladi).
        val initialBottom = binding.bottomBarContainer.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomBarContainer) { view, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            view.updatePadding(bottom = initialBottom + bottom)
            insets
        }
    }
}