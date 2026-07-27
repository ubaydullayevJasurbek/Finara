package com.ubaydullayev.expensetracker.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ubaydullayev.expensetracker.databinding.ScreenHomeBinding
import com.ubaydullayev.expensetracker.presentation.common.BaseViewModelFragment

class HomeScreen : BaseViewModelFragment<ScreenHomeBinding, HomeViewModel>(HomeViewModel::class.java) {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenHomeBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // `viewModel` (HomeViewModel) and `binding` (ScreenHomeBinding) are ready to use here,
        // and edge-to-edge insets are already applied by BaseFragment.
    }
}
