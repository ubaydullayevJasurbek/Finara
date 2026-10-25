
package com.example.finara.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt

class BudgetProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(16f)
        color = "#356F5C".toColorInt()
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(16f)
        strokeCap = Paint.Cap.BUTT
        color = "#B7F8DF".toColorInt()
    }

    var progress: Float = 68f
        set(value) {
            field = value.coerceIn(0f, 100f)
            invalidate()
        }

    private val bounds = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val inset = trackPaint.strokeWidth / 2f + dp(2f)

        bounds.set(
            inset,
            inset,
            width - inset,
            height - inset
        )

        // Orqa doira
        canvas.drawArc(
            bounds,
            0f,
            360f,
            false,
            trackPaint
        )

        // Sarflangan budjet foizi
        canvas.drawArc(
            bounds,
            -90f,
            360f * progress / 100f,
            false,
            progressPaint
        )
    }

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density
}
