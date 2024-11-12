package com.example.utils

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import kotlin.math.max

class VerticalSeekBarWrapper @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context!!, attrs, defStyleAttr
) {
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (useViewRotation()) {
            onSizeChangedUseViewRotation(w, h, oldw, oldh)
        } else {
            onSizeChangedTraditionalRotation(w, h, oldw, oldh)
        }
    }

    private fun onSizeChangedTraditionalRotation(w: Int, h: Int, oldw: Int, oldh: Int) {
        val seekBar = childSeekBar

        if (seekBar != null) {
            val hPadding = paddingLeft + paddingRight
            val vPadding = paddingTop + paddingBottom
            val lp = seekBar.layoutParams as LayoutParams

            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.height = max(0.0, (h - vPadding).toDouble()).toInt()
            seekBar.layoutParams = lp

            seekBar.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)

            val seekBarMeasuredWidth = seekBar.measuredWidth
            seekBar.measure(
                MeasureSpec.makeMeasureSpec(
                    max(0.0, (w - hPadding).toDouble()).toInt(),
                    MeasureSpec.AT_MOST
                ),
                MeasureSpec.makeMeasureSpec(
                    max(0.0, (h - vPadding).toDouble()).toInt(),
                    MeasureSpec.EXACTLY
                )
            )

            lp.gravity = Gravity.TOP or Gravity.LEFT
            lp.leftMargin =
                ((max(0.0, (w - hPadding).toDouble()) - seekBarMeasuredWidth) / 2).toInt()
            seekBar.layoutParams = lp
        }

        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun onSizeChangedUseViewRotation(w: Int, h: Int, oldw: Int, oldh: Int) {
        val seekBar = childSeekBar

        if (seekBar != null) {
            val hPadding = paddingLeft + paddingRight
            val vPadding = paddingTop + paddingBottom
            seekBar.measure(
                MeasureSpec.makeMeasureSpec(
                    max(0.0, (h - vPadding).toDouble()).toInt(),
                    MeasureSpec.EXACTLY
                ),
                MeasureSpec.makeMeasureSpec(
                    max(0.0, (w - hPadding).toDouble()).toInt(),
                    MeasureSpec.AT_MOST
                )
            )
        }

        applyViewRotation(w, h)
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val seekBar = childSeekBar
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        if ((seekBar != null) && (widthMode != MeasureSpec.EXACTLY)) {
            val seekBarWidth: Int
            val seekBarHeight: Int
            val hPadding = paddingLeft + paddingRight
            val vPadding = paddingTop + paddingBottom
            val innerContentWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                max(0.0, (widthSize - hPadding).toDouble()).toInt(), widthMode
            )
            val innerContentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                max(0.0, (heightSize - vPadding).toDouble()).toInt(), heightMode
            )

            if (useViewRotation()) {
                seekBar.measure(innerContentHeightMeasureSpec, innerContentWidthMeasureSpec)
                seekBarWidth = seekBar.measuredHeight
                seekBarHeight = seekBar.measuredWidth
            } else {
                seekBar.measure(innerContentWidthMeasureSpec, innerContentHeightMeasureSpec)
                seekBarWidth = seekBar.measuredWidth
                seekBarHeight = seekBar.measuredHeight
            }

            val measuredWidth = resolveSizeAndState(seekBarWidth + hPadding, widthMeasureSpec, 0)
            val measuredHeight = resolveSizeAndState(seekBarHeight + vPadding, heightMeasureSpec, 0)

            setMeasuredDimension(measuredWidth, measuredHeight)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    /*package*/
    fun applyViewRotation() {
        applyViewRotation(width, height)
    }

    private fun applyViewRotation(w: Int, h: Int) {
        val seekBar = childSeekBar

        if (seekBar != null) {
            val isLTR = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR
            val rotationAngle = seekBar.rotationAngle
            val seekBarMeasuredWidth = seekBar.measuredWidth
            val seekBarMeasuredHeight = seekBar.measuredHeight
            val hPadding = paddingLeft + paddingRight
            val vPadding = paddingTop + paddingBottom
            val hOffset =
                ((max(0.0, (w - hPadding).toDouble()) - seekBarMeasuredHeight) * 0.5f).toFloat()
            val lp = seekBar.layoutParams

            lp.width = max(0.0, (h - vPadding).toDouble()).toInt()
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT

            seekBar.layoutParams = lp

            seekBar.pivotX = (if ((isLTR)) 0 else max(0.0, (h - vPadding).toDouble())).toFloat()
            seekBar.pivotY = 0f

            when (rotationAngle) {
                VerticalSeekBar.ROTATION_ANGLE_CW_90 -> {
                    seekBar.rotation = 90f
                    if (isLTR) {
                        seekBar.translationX = seekBarMeasuredHeight + hOffset
                        seekBar.translationY = 0f
                    } else {
                        seekBar.translationX = -hOffset
                        seekBar.translationY = seekBarMeasuredWidth.toFloat()
                    }
                }

                VerticalSeekBar.ROTATION_ANGLE_CW_270 -> {
                    seekBar.rotation = 270f
                    if (isLTR) {
                        seekBar.translationX = hOffset
                        seekBar.translationY = seekBarMeasuredWidth.toFloat()
                    } else {
                        seekBar.translationX = -(seekBarMeasuredHeight + hOffset)
                        seekBar.translationY = 0f
                    }
                }
            }
        }
    }

    private val childSeekBar: VerticalSeekBar?
        get() {
            val child = if ((childCount > 0)) getChildAt(0) else null
            return if ((child is VerticalSeekBar)) child else null
        }

    private fun useViewRotation(): Boolean {
        val seekBar = childSeekBar
        return seekBar?.useViewRotation() ?: false
    }
}