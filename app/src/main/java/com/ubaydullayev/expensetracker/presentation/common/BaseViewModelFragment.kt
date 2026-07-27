package com.ubaydullayev.expensetracker.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

/**
 * [BaseFragment] plus a lifecycle-scoped [ViewModel]. Kept separate from [BaseFragment] on purpose:
 * screens that don't need a ViewModel (splash, empty placeholders) shouldn't be forced to declare one
 * (SOLID: interface segregation).
 *
 * Usage:
 * ```
 * class HomeScreen : BaseViewModelFragment<ScreenHomeBinding, HomeViewModel>(HomeViewModel::class.java) {
 *     override fun inflateBinding(i, c) = ScreenHomeBinding.inflate(i, c, false)
 * }
 * ```
 *
 * The ViewModel is created lazily and scoped to this Fragment, so it survives configuration changes
 * and is cleared automatically when the Fragment is destroyed. Override [viewModelFactory] to supply
 * a custom factory (e.g. to pass constructor arguments); otherwise the default factory is used.
 */
abstract class BaseViewModelFragment<VB : ViewBinding, VM : ViewModel>(
    private val viewModelClass: Class<VM>,
) : BaseFragment<VB>() {

    /** Override to inject a custom factory. Null -> the Fragment's default factory. */
    protected open val viewModelFactory: ViewModelProvider.Factory? = null

    /** Scoped to this Fragment. Safe to use from onViewCreated onwards. */
    protected val viewModel: VM by lazy(LazyThreadSafetyMode.NONE) {
        val provider = viewModelFactory
            ?.let { ViewModelProvider(this, it) }
            ?: ViewModelProvider(this)
        provider[viewModelClass]
    }
}
