package com.ubaydullayev.expensetracker.presentation.splash

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.databinding.ScreenSplashBinding
import com.ubaydullayev.expensetracker.presentation.common.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

@SuppressLint("CustomSplashScreen")
class SplashScreen : BaseFragment<ScreenSplashBinding>() {

    // Immersive splash: draw fully behind the transparent bars, and use white icons on the green bg.
    override val applySystemBarInsets: Boolean = false
    override val lightStatusBars: Boolean = false

    // Held only so they can be cancelled in onDestroyView (they reference views -> avoid leaks).
    private var logoAnimator: Animator? = null
    private var progressAnimator: ObjectAnimator? = null

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenSplashBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Binding + edge-to-edge (immersive) + white status icons are handled by BaseFragment.
        super.onViewCreated(view, savedInstanceState)

        // Defensive initial state (also set in XML so the very first frame never flashes the
        // full-size opaque logo before the animation's first frame runs).
        binding.logoGroup.apply {
            alpha = 0f
            scaleX = LOGO_START_SCALE
            scaleY = LOGO_START_SCALE
        }

        // A single coroutine bound to the *view* lifecycle. It is cancelled automatically in
        // onDestroyView (config change / back), so it can never navigate on a dead view.
        viewLifecycleOwner.lifecycleScope.launch {
            // Preload the flag off the main thread first: SharedPreferences' first access hits disk.
            // It's fast, so doing it before the animation removes any progress/read race entirely.
            val onboardingDone = withContext(Dispatchers.IO) { readOnboardingDone() }

            // Logo starts first (fire-and-forget, ~800ms).
            animateLogo()

            // Progress starts just after the logo begins and eases smoothly 0 -> 100.
            // Navigation is gated on it reaching 100%, so it defines the total splash length.
            val progress = ObjectAnimator.ofInt(binding.pbLoading, "progress", 0, 100).apply {
                duration = PROGRESS_DURATION_MS
                startDelay = PROGRESS_START_DELAY_MS
                interpolator = AccelerateDecelerateInterpolator() // gentle ease-in-out, no jump
            }
            progressAnimator = progress

            progress.start()
            progress.awaitEnd() // suspends until the bar actually reaches 100%

            // Reached here only on a natural finish while the view is alive -> navigate exactly once.
            navigateAway(onboardingDone)
        }
    }

    private fun animateLogo() {
        val logo = binding.logoGroup

        val fadeIn = ObjectAnimator.ofFloat(logo, View.ALPHA, 0f, 1f).apply {
            duration = LOGO_FADE_MS
            interpolator = DecelerateInterpolator()
        }
        val scaleX = ObjectAnimator.ofFloat(logo, View.SCALE_X, LOGO_START_SCALE, 1f)
        val scaleY = ObjectAnimator.ofFloat(logo, View.SCALE_Y, LOGO_START_SCALE, 1f)
        val overshoot = OvershootInterpolator(LOGO_OVERSHOOT_TENSION)

        logoAnimator = AnimatorSet().apply {
            duration = LOGO_SCALE_MS
            interpolator = overshoot
            // AnimatorSet applies its duration/interpolator to children that don't set their own;
            // fadeIn keeps its own (shorter, decelerate), the scale animators inherit the overshoot.
            playTogether(fadeIn, scaleX, scaleY)
            start()
        }
    }

    private fun readOnboardingDone(): Boolean =
        requireContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ONBOARDING_DONE, false)

    private fun navigateAway(onboardingDone: Boolean) {
        val navController = findNavController()
        // Guard against navigating twice / from the wrong destination (would throw with popUpToInclusive).
        if (navController.currentDestination?.id != R.id.splashScreen) return

        // The fade in/out crossfade is declared on the actions in nav_graph.xml.
        navController.navigate(
            if (onboardingDone) R.id.action_splashScreen_to_loginScreen
            else R.id.action_splashScreen_to_onboardingScreen
        )
    }

    override fun onDestroyView() {
        logoAnimator?.cancel()
        logoAnimator = null
        progressAnimator?.cancel()
        progressAnimator = null
        // BaseFragment clears the binding.
        super.onDestroyView()
    }


    private suspend fun Animator.awaitEnd() = suspendCancellableCoroutine { cont ->
        val listener = object : AnimatorListenerAdapter() {
            private var cancelled = false
            override fun onAnimationCancel(animation: Animator) {
                cancelled = true
            }
            override fun onAnimationEnd(animation: Animator) {
                animation.removeListener(this)
                if (!cancelled && cont.isActive) cont.resume(Unit)
            }
        }
        addListener(listener)
        cont.invokeOnCancellation {
            removeListener(listener)
            cancel()
        }
    }

    private companion object {
        // Logo: finishes in ~800ms (within the 700-900ms target).
        const val LOGO_START_SCALE = 0.85f
        const val LOGO_FADE_MS = 450L
        const val LOGO_SCALE_MS = 800L
        const val LOGO_OVERSHOOT_TENSION = 1.5f // subtle ~5% overshoot, not a heavy bounce

        // Progress starts just after the logo, ends ~1400ms in (within the 1200-1800ms target).
        const val PROGRESS_START_DELAY_MS = 150L
        const val PROGRESS_DURATION_MS = 1250L

        const val PREFS_NAME = "app"
        const val KEY_ONBOARDING_DONE = "onboarding_done"
    }
}
