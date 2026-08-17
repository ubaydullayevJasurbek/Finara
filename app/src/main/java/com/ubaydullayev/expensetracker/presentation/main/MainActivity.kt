package com.ubaydullayev.expensetracker.presentation.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val tabDestinations = mapOf(
        0 to R.id.homeScreen,
        1 to R.id.transactionScreen,
        3 to R.id.budgetScreen,
        4 to R.id.profileScreen,
    )
    private val destinationToTab =
        tabDestinations.entries.associate { (index, dest) -> dest to index }

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
    }

    private fun setupBottomBar() {
        binding.bottomNavigation.onItemSelected = { index ->
            when (val destination = tabDestinations[index]) {
                null -> openAddFlow()
                else -> navigateToTab(destination)
            }
        }


        navController.addOnDestinationChangedListener { _, destination, _ ->
            val tabIndex = destinationToTab[destination.id]
            binding.bottomBarContainer.isVisible = tabIndex != null
            if (tabIndex != null) binding.bottomNavigation.setActiveItem(tabIndex)
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

    private fun applyBottomBarInset() {

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomBarContainer) { view, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            view.updatePadding(bottom = bottom)
            insets
        }
    }
}
