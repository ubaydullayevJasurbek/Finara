package com.ubaydullayev.expensetracker.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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


    }
}
