package com.ubaydullayev.expensetracker.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpaceDecoration(private val spaceDp: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val space = (spaceDp * view.context.resources.displayMetrics.density).toInt()
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        if (position != itemCount -1){
            outRect.right = space
        }
    }
}