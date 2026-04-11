package com.example.flashmind.feature.deck

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnRepeat
import kotlin.math.min

class StudyRadarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = context.resources.displayMetrics.density * 2f
        color = 0x331B666F.toInt()
    }

    private val pulsePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0x77DAA94D.toInt()
    }

    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFF102A43.toInt()
    }

    private var phase: Float = 0f

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 2400L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            phase = it.animatedValue as Float
            invalidate()
        }
        doOnRepeat {
            phase = 0f
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!animator.isStarted) animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val maxRadius = min(width, height) * 0.42f

        for (index in 1..3) {
            canvas.drawCircle(cx, cy, maxRadius * index / 3f, ringPaint)
        }

        val pulseRadius = maxRadius * (0.28f + phase * 0.72f)
        pulsePaint.alpha = ((1f - phase) * 160).toInt().coerceIn(40, 160)
        canvas.drawCircle(cx, cy, pulseRadius, pulsePaint)
        canvas.drawCircle(cx, cy, maxRadius * 0.18f, corePaint)
    }
}
