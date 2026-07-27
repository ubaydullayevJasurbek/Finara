package com.ubaydullayev.expensetracker.presentation.splash

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.adapter.OnboardingAdapter
import com.ubaydullayev.expensetracker.adapter.OnboardingPage
import com.ubaydullayev.expensetracker.databinding.ScreenOnboardingBinding
import com.ubaydullayev.expensetracker.presentation.common.BaseFragment
import kotlin.collections.listOf
import androidx.core.content.edit


class OnboardingScreen : BaseFragment<ScreenOnboardingBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenOnboardingBinding.inflate(inflater, container, false)

    private val pages by lazy {

        listOf(
            OnboardingPage(
                R.drawable.img_onboarding_1,
                "Take control of your money",
                "Smart budgets, automatic tracking, and AI insights that help you save more — all in one beautiful app."
            ),
            OnboardingPage(
                R.drawable.img_onboarding_2,
                "Budgets that keep you on track",
                "Set limits for each category and get gentle alerts before you overspend."
            ),
            OnboardingPage(
                R.drawable.img_onboarding_3,
                "Smart insights, powered by AI",
                "Get personalized tips to spend less and grow your savings faster."
            )
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Edge-to-edge insets + dark status icons (light background) are applied by BaseFragment.
        super.onViewCreated(view, savedInstanceState)

        binding.viewPager.adapter = OnboardingAdapter(pages)
        binding.viewPager.isUserInputEnabled = false

        binding.tabIndicator.attachTo(binding.viewPager)

        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val isLast = position == pages.lastIndex
                    binding.btnGetStarted.setText(
                        if (isLast) R.string.get_started else R.string.get_continue
                    )
                    binding.btnSkip.visibility = if (isLast) View.INVISIBLE else View.VISIBLE
                }
            }
        )
        binding.btnGetStarted.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < pages.lastIndex) {
                binding.viewPager.currentItem = current + 1
            } else {
                finishOnboarding()
            }
        }

        binding.btnSkip.setOnClickListener { finishOnboarding() }
    }

    private fun finishOnboarding() {
        requireContext()
            .getSharedPreferences("app", Context.MODE_PRIVATE)
            .edit { putBoolean("onboarding_done", true) }

        findNavController().navigate(R.id.action_onboardingScreen_to_loginScreen)
    }
}

