package com.ubaydullayev.expensetracker.presentation.splash

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.adapter.OnboardingAdapter
import com.ubaydullayev.expensetracker.adapter.OnboardingPage
import com.ubaydullayev.expensetracker.databinding.ScreenOnboardingBinding
import com.ubaydullayev.expensetracker.presentation.common.BaseFragment

class OnboardingScreen : BaseFragment<ScreenOnboardingBinding>() {

    // The green illustration panel bleeds under the status bar (white icons), while the bottom
    // still clears the navigation bar.
    override val insetTop: Boolean = false
    override val lightStatusBars: Boolean = false

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenOnboardingBinding.inflate(inflater, container, false)

    private val pages by lazy {
        listOf(
            OnboardingPage(
                panelBg = R.drawable.bg_onb_panel_1,
                glyph = R.drawable.ic_onb_track,
                chip = "TRACK",
                chipBg = R.color.onb_tint_green,
                chipText = R.color.onb_brand_primary,
                pill1 = "+$5,240", pill1Dot = R.color.onb_pill_green,
                pill2 = "−$84.20", pill2Dot = R.color.onb_pill_coral,
                title = "Every transaction,\nbeautifully organized",
                subtitle = "Automatically categorize your spending and see exactly where your money goes.",
            ),
            OnboardingPage(
                panelBg = R.drawable.bg_onb_panel_2,
                glyph = R.drawable.ic_onb_budget,
                chip = "BUDGET",
                chipBg = R.color.onb_tint_blue,
                chipText = R.color.onb_accent_blue,
                pill1 = "76% used", pill1Dot = R.color.onb_pill_blue,
                pill2 = "On track", pill2Dot = R.color.onb_pill_green,
                title = "Set limits that\nactually stick",
                subtitle = "Smart budgets adapt to your habits and warn you before you overspend.",
            ),
            OnboardingPage(
                panelBg = R.drawable.bg_onb_panel_3,
                glyph = R.drawable.ic_onb_ai,
                chip = "AI INSIGHTS",
                chipBg = R.color.onb_tint_purple,
                chipText = R.color.onb_accent_purple,
                pill1 = "Save $276", pill1Dot = R.color.onb_pill_purple,
                pill2 = "Insight", pill2Dot = R.color.onb_pill_teal,
                title = "Your money,\nunderstood",
                subtitle = "AI spots patterns, hidden subscriptions and savings you'd never notice alone.",
            ),
            OnboardingPage(
                panelBg = R.drawable.bg_onb_panel_4,
                glyph = R.drawable.ic_onb_goals,
                chip = "GOALS",
                chipBg = R.color.onb_tint_green,
                chipText = R.color.onb_brand_primary,
                pill1 = "68% saved", pill1Dot = R.color.onb_pill_green,
                pill2 = "Goal hit!", pill2Dot = R.color.onb_pill_amber,
                title = "Reach every goal\nyou set",
                subtitle = "Turn intentions into milestones — and celebrate every one you cross.",
            ),
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPager.adapter = OnboardingAdapter(pages)
        binding.viewPager.isUserInputEnabled = false
        binding.tabIndicator.attachTo(binding.viewPager)

        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    bindChrome(position)
                }
            }
        )
        bindChrome(binding.viewPager.currentItem)

        binding.btnGetStarted.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < pages.lastIndex) {
                binding.viewPager.currentItem = current + 1
            } else {
                finishOnboarding()
            }
        }
        binding.btnSkip.setOnClickListener { finishOnboarding() }
        binding.txtSignIn.setOnClickListener { finishOnboarding() }
    }

    /** Skip / CTA label / sign-in row depend on whether we're on the last page. */
    private fun bindChrome(position: Int) {
        val isLast = position == pages.lastIndex
        binding.ctaText.setText(if (isLast) R.string.get_started else R.string.onb_next)
        binding.btnSkip.visibility = if (isLast) View.INVISIBLE else View.VISIBLE
        binding.signInRow.visibility = if (isLast) View.VISIBLE else View.GONE
    }

    private fun finishOnboarding() {
        requireContext()
            .getSharedPreferences("app", Context.MODE_PRIVATE)
            .edit { putBoolean("onboarding_done", true) }

        findNavController().navigate(R.id.action_onboardingScreen_to_loginScreen)
    }
}
