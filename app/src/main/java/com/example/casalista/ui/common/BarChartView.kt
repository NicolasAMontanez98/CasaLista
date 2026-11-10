package com.example.casalista.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.example.casalista.R

class BarChartView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    var bars: List<Pair<String, Long>> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = context.getColor(R.color.gold) }
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.text_secondary)
        textAlign = Paint.Align.CENTER
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (bars.isEmpty()) return
        val max = bars.maxOf { it.second }.coerceAtLeast(1)
        val text = textPaint.textSize
        val slot = width / bars.size.toFloat()
        val barWidth = slot * 0.5f
        val bottom = height - text * 2
        val maxHeight = bottom - text * 2

        bars.forEachIndexed { i, (label, value) ->
            val center = slot * i + slot / 2
            val top = bottom - maxHeight * value / max
            canvas.drawRoundRect(center - barWidth / 2, top, center + barWidth / 2, bottom, 12f, 12f, barPaint)
            canvas.drawText("${value / 1_000_000}M", center, top - text / 2, textPaint)
            val fitted = TextUtils.ellipsize(label, textPaint, slot, TextUtils.TruncateAt.END)
            canvas.drawText(fitted.toString(), center, bottom + text * 1.4f, textPaint)
        }
    }
}
