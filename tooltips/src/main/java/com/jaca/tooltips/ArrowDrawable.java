package com.jaca.tooltips;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;

/**
 * ArrowDrawable
 *
 * @author Created by zjn on 31/07/21.
 */
public class ArrowDrawable extends ColorDrawable {

    public static final int LEFT = 0, TOP = 1, RIGHT = 2, BOTTOM = 3, AUTO = 4;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int mBackgroundColor;
    private Path mPath;
    private final int mDirection;

    private final Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    ArrowDrawable(@ColorInt int foregroundColor, int direction, @ColorInt int strokeColor, @Dimension int strokeWidth) {
        this.mBackgroundColor = Color.TRANSPARENT;
        this.mPaint.setColor(foregroundColor);
        this.mDirection = direction;

        mStrokePaint.setColor(strokeColor);
        mStrokePaint.setStrokeWidth(strokeWidth);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        updatePath(bounds);
    }

    private synchronized void updatePath(Rect bounds) {
        mPath = new Path();

        switch (mDirection) {
            case LEFT:
                mPath.moveTo(bounds.width(), bounds.height());
                mPath.lineTo(0, bounds.height() / 2);
                mPath.lineTo(bounds.width(), 0);
                mPath.lineTo(bounds.width(), bounds.height());
                break;
            case TOP:
                mPath.moveTo(0, bounds.height());
                mPath.lineTo(bounds.width() / 2, 0);
                mPath.lineTo(bounds.width(), bounds.height());
                mPath.lineTo(0, bounds.height());
                break;
            case RIGHT:
                mPath.moveTo(0, 0);
                mPath.lineTo(bounds.width(), bounds.height() / 2);
                mPath.lineTo(0, bounds.height());
                mPath.lineTo(0, 0);
                break;
            case BOTTOM:
                mPath.moveTo(0, 0);
                mPath.lineTo(bounds.width() / 2, bounds.height());
                mPath.lineTo(bounds.width(), 0);
                mPath.lineTo(0, 0);
                break;
        }

        mPath.close();
    }

    private void drawStroke(Canvas canvas, Rect bounds) {
        final int w = bounds.width();
        final int h = bounds.height();
        switch (mDirection) {
            case LEFT:
                canvas.drawLine(w, h, 0, h >> 1, mStrokePaint);
                canvas.drawLine(0, h >> 1, w, 0, mStrokePaint);
                break;
            case TOP:
                canvas.drawLine(0, h, w >> 1, 0, mStrokePaint);
                canvas.drawLine(w >> 1, 0, w, h, mStrokePaint);
                break;
            case RIGHT:
                canvas.drawLine(0, 0, w, h >> 1, mStrokePaint);
                canvas.drawLine(w, h >> 1, 0, h, mStrokePaint);
                break;
            case BOTTOM:
                canvas.drawLine(0, 0, w >> 1, h, mStrokePaint);
                canvas.drawLine(w >> 1, h, w, 0, mStrokePaint);
                break;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawColor(mBackgroundColor);
        if (mPath == null)
            updatePath(getBounds());
        canvas.drawPath(mPath, mPaint);
        drawStroke(canvas, getBounds());
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    public void setColor(@ColorInt int color) {
        mPaint.setColor(color);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        if (mPaint.getColorFilter() != null) {
            return PixelFormat.TRANSLUCENT;
        }

        switch (mPaint.getColor() >>> 24) {
            case 255:
                return PixelFormat.OPAQUE;
            case 0:
                return PixelFormat.TRANSPARENT;
        }
        return PixelFormat.TRANSLUCENT;
    }
}