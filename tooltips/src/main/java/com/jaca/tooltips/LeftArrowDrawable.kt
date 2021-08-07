package com.jaca.tooltips

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

class LeftArrowDrawable : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var path: Path? = null

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        updatePath(bounds)
    }

    @Synchronized
    private fun updatePath(bounds: Rect) {
        path = Path().also {
            it.moveTo(bounds.width().toFloat(), bounds.height().toFloat())
            it.lineTo(0f, bounds.height() / 2.toFloat())
            it.lineTo(bounds.width().toFloat(), 0f)
            it.lineTo(bounds.width().toFloat(), bounds.height().toFloat())
            it.close()
        }
    }

    private fun drawStroke(
        canvas: Canvas,
        bounds: Rect
    ) {
        val w = bounds.width()
        val h = bounds.height()
        canvas.drawLine(
            w.toFloat(), h.toFloat(), 0f,
            (h shr 1.toFloat().toInt()).toFloat(), paint
        )
        canvas.drawLine(0f, (h shr 1.toFloat().toInt()).toFloat(), w.toFloat(), 0f, paint)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawColor(Color.TRANSPARENT)
        if (path == null) updatePath(bounds)
        canvas.drawPath(path!!, paint)
        drawStroke(canvas, bounds)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    fun setColor(@ColorInt color: Int) {
        paint.color = color
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        if (paint.colorFilter != null) {
            return PixelFormat.TRANSLUCENT
        }
        when (paint.color ushr 24) {
            255 -> return PixelFormat.OPAQUE
            0 -> return PixelFormat.TRANSPARENT
        }
        return PixelFormat.TRANSLUCENT
    }
}