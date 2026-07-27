package com.ubaydullayev.expensetracker.presentation.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null

    /** Valid only between [onCreateView] and [onDestroyView] (i.e. while the view exists). */
    protected val binding: VB
        get() = _binding
            ?: error("binding is only valid between onCreateView and onDestroyView")

    /** Create the concrete binding, e.g. `ScreenXBinding.inflate(inflater, container, false)`. */
    protected abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    protected open val applySystemBarInsets: Boolean = true
    protected open val lightStatusBars: Boolean = true

    protected open val insetLeft: Boolean = true
    protected open val insetTop: Boolean = true
    protected open val insetRight: Boolean = true
    protected open val insetBottom: Boolean = true

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = inflateBinding(inflater, container)
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (applySystemBarInsets) {
            view.applySystemBarInsetsAsPadding(
                applyLeft = insetLeft,
                applyTop = insetTop,
                applyRight = insetRight,
                applyBottom = insetBottom,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Set here (not onViewCreated) so the icon color is re-applied whenever this screen becomes
        // the visible one again — e.g. after navigating back from another destination.
        setLightStatusBars(lightStatusBars)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
