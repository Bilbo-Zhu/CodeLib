package com.jaca.tooltips;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

/**
 * @author Created by zjn on 31/07/21.
 * @see PopupWindow
 */
@SuppressWarnings("SameParameterValue")
public class SimpleTooltip implements PopupWindow.OnDismissListener {

    private static final String TAG = SimpleTooltip.class.getSimpleName();

    // Default Resources
    private static final int DEFAULT_POPUP_WINDOW_STYLE_RES = android.R.attr.popupWindowStyle;
    private static final int DEFAULT_TEXT_APPEARANCE_RES = R.style.SimpleTooltipDefault;
    private static final int DEFAULT_BACKGROUND_COLOR_RES = R.color.simpletooltip_background;
    private static final int DEFAULT_TEXT_COLOR_RES = R.color.simpletooltip_text;
    private static final int DEFAULT_ARROW_COLOR_RES = R.color.simpletooltip_arrow;
    private static final int DEFAULT_MARGIN_RES = R.dimen.simple_tooltip_margin;
    private static final int DEFAULT_POP_WINDOW_MARGIN_RES = R.dimen.simple_tooltip_margin;
    private static final int DEFAULT_PADDING_RES = R.dimen.simple_tooltip_padding;
    private static final int DEFAULT_ANIMATION_PADDING_RES = R.dimen.simple_tooltip_animation_padding;
    private static final int DEFAULT_ANIMATION_DURATION_RES = R.integer.simple_tooltip_animation_duration;
    private static final int DEFAULT_ARROW_WIDTH_RES = R.dimen.simpletooltip_arrow_width;
    private static final int DEFAULT_ARROW_HEIGHT_RES = R.dimen.simpletooltip_arrow_height;
    private static final int DEFAULT_OVERLAY_OFFSET_RES = R.dimen.simpletooltip_overlay_offset;
    public static final int PIN_RULES_GRAVITY = 123;
    public static final int BOTTOM_LEFT_GRAVITY = 456;

    private final Context mContext;
    private OnDismissListener mOnDismissListener;
    private OnShowListener mOnShowListener;
    private PopupWindow mPopupWindow;
    private final int mGravity;
    private final int mArrowDirection;
    private final boolean mDismissOnInsideTouch;
    private boolean mDismissOnOutsideTouch;
    private final boolean mModal;
    private final View mContentView;
    private View mContentLayout;
    @IdRes
    private final int mTextViewId;
    private final CharSequence mText;
    private final View mAnchorView;
    private final boolean mTransparentOverlay;
    private final float mOverlayOffset;
    private final float mMaxWidth;
    private View mOverlay;
    private ViewGroup mRootView;
    private final boolean mShowArrow;
    private ImageView mArrowView;
    private final Drawable mArrowDrawable;
    private final boolean mAnimated;
    private AnimatorSet mAnimator;
    private final float mMargin;
    private final float mNegativeMargin;
    private final float mPopWindowMargin;
    private final float mPadding;
    private final int mLeftPadding;
    private final int mTopPadding;
    private final int mRightPadding;
    private final int mBottomPadding;
    private final float mAnimationPadding;
    private final long mAnimationDuration;
    private final float mArrowWidth;
    private final float mArrowHeight;
    private final boolean mFocusable;
    private boolean dismissed = false;
    private boolean keepInsideScreen = false;
    private int mHighlightShape = OverlayView.HIGHLIGHT_SHAPE_OVAL;

    private SimpleTooltip(Builder builder) {
        mContext = builder.context;
        mGravity = builder.gravity;
        mArrowDirection = builder.arrowDirection;
        mDismissOnInsideTouch = builder.dismissOnInsideTouch;
        mDismissOnOutsideTouch = builder.dismissOnOutsideTouch;
        mModal = builder.modal;
        mContentView = builder.contentView;
        mTextViewId = builder.textViewId;
        mText = builder.text;
        mAnchorView = builder.anchorView;
        mTransparentOverlay = builder.transparentOverlay;
        mOverlayOffset = builder.overlayOffset;
        mMaxWidth = builder.maxWidth;
        mShowArrow = builder.showArrow;
        mArrowWidth = builder.arrowWidth;
        mArrowHeight = builder.arrowHeight;
        mArrowDrawable = builder.arrowDrawable;
        mAnimated = builder.animated;
        mMargin = builder.margin;
        mNegativeMargin = builder.mNegativeMargin;
        mPopWindowMargin = builder.popWindowMargin;
        mPadding = builder.padding;
        mLeftPadding = builder.leftPadding;
        mTopPadding = builder.topPadding;
        mRightPadding = builder.rightPadding;
        mBottomPadding = builder.bottomPadding;
        mAnimationPadding = builder.animationPadding;
        mAnimationDuration = builder.animationDuration;
        mOnDismissListener = builder.onDismissListener;
        mOnShowListener = builder.onShowListener;
        mFocusable = builder.focusable;
        mRootView = (ViewGroup) mAnchorView.getRootView();
        mHighlightShape = builder.highlightShape;
        keepInsideScreen = builder.keepInsideScreen;

        init();
    }

    private void init() {
        configPopupWindow();
        configContentView();
    }

    private void configPopupWindow() {
        mPopupWindow = new PopupWindow(mContext, null, DEFAULT_POPUP_WINDOW_STYLE_RES);
        mPopupWindow.setOnDismissListener(this);
        mPopupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.setClippingEnabled(false);
        mPopupWindow.setFocusable(mFocusable);
    }


    public void show() {
        verifyDismissed();
        mContentLayout.getViewTreeObserver().addOnGlobalLayoutListener(mLocationLayoutListener);
        mContentLayout.getViewTreeObserver().addOnGlobalLayoutListener(mAutoDismissLayoutListener);

        mRootView.post(new Runnable() {
            @Override
            public void run() {
                if (mRootView.isShown() && mPopupWindow != null)
                    mPopupWindow.showAtLocation(mRootView, Gravity.NO_GRAVITY, mRootView.getWidth(), mRootView.getHeight());
                else
                    Log.e(TAG, "Tooltip cannot be shown, root view is invalid or has been closed.");
            }
        });
    }

    private void verifyDismissed() {
        if (dismissed) {
            throw new IllegalArgumentException("Tooltip has ben dismissed.");
        }
    }

    private void createOverlay() {
        mOverlay = mTransparentOverlay ? new View(mContext) : new OverlayView(mContext, mAnchorView, mHighlightShape, mOverlayOffset);
        mOverlay.setId(R.id.tooltip_overlay);
        mOverlay.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mOverlay.setOnTouchListener(mOverlayTouchListener);
        mRootView.addView(mOverlay);
    }

    public void setOverlayTouchable(Boolean touchable) {
        mDismissOnOutsideTouch = touchable;
    }

    private PointF calculePopupLocation() {
        PointF location = new PointF();

        final RectF anchorRect = SimpleTooltipUtils.calculeRectInWindow(mAnchorView);
        final PointF anchorCenter = new PointF(anchorRect.centerX(), anchorRect.centerY());

        switch (mGravity) {
            case Gravity.START:
                location.x = anchorRect.left - mPopupWindow.getContentView().getWidth() - mMargin;
                location.y = anchorCenter.y - mPopupWindow.getContentView().getHeight() / 2f;
                break;
            case Gravity.END:
                location.x = anchorRect.right + mMargin;
                location.y = anchorCenter.y - mPopupWindow.getContentView().getHeight() / 2f;
                break;
            case Gravity.TOP:
                final int windowWidth = mContext.getResources().getDisplayMetrics().widthPixels;
                final int popWindowWidth = mPopupWindow.getContentView().getWidth();
                final float windowMargin = mPopWindowMargin;
                float x = anchorCenter.x - (popWindowWidth >> 1);
                if (x < windowMargin) {
                    x = windowMargin;
                }
                if (x + popWindowWidth + windowMargin > windowWidth) {
                    x = windowWidth - windowMargin - popWindowWidth;
                }
                location.x = x;
                location.y = anchorRect.top - mPopupWindow.getContentView().getHeight() - mMargin;
                break;
            case PIN_RULES_GRAVITY:
                location.x = anchorCenter.x - mPopupWindow.getContentView().getWidth() + (6 * mMargin); // should be aligned with actionbar back button
                location.y = anchorRect.top - mPopupWindow.getContentView().getHeight() - mMargin;
                break;
            case Gravity.BOTTOM:
                location.x = anchorCenter.x - mPopupWindow.getContentView().getWidth() / 2f;
                location.y = anchorRect.bottom + mMargin + mNegativeMargin;
                break;
            case Gravity.CENTER:
                location.x = anchorCenter.x - mPopupWindow.getContentView().getWidth() / 2f;
                location.y = anchorCenter.y - mPopupWindow.getContentView().getHeight() / 2f;
                break;
            case BOTTOM_LEFT_GRAVITY:
                location.x = anchorCenter.x - mPopupWindow.getContentView().getWidth();
                location.y = anchorRect.bottom;
                break;
            default:
                throw new IllegalArgumentException("Gravity must have be CENTER, START, END, TOP or BOTTOM.");
        }

        if (keepInsideScreen) {
            if (location.x < 0) {
                location.x = 0;
            }
            if (location.y < 0) {
                location.y = 0;
            }
            if (location.x + mPopupWindow.getContentView().getWidth() + mMargin >= mRootView.getWidth()) {
                location.x = mRootView.getWidth() - mPopupWindow.getContentView().getWidth() - mMargin;
            }
        }
        return location;
    }

    private void configContentView() {
        if (mContentView instanceof TextView) {
            TextView tv = (TextView) mContentView;
            tv.setText(mText);
        } else {
            TextView tv = (TextView) mContentView.findViewById(mTextViewId);
            if (tv != null)
                tv.setText(mText);
        }

        if(mLeftPadding > 0 && mTopPadding > 0 && mRightPadding > 0 && mBottomPadding > 0) {
            mContentView.setPadding(mLeftPadding, mTopPadding, mRightPadding, mBottomPadding);
        } else {
            mContentView.setPadding((int) mPadding, (int) mPadding, (int) mPadding, (int) mPadding);
        }


        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(mArrowDirection == ArrowDrawable.LEFT || mArrowDirection == ArrowDrawable.RIGHT ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        int layoutPadding = (int) (mAnimated ? mAnimationPadding : 0);
        linearLayout.setPadding(layoutPadding, layoutPadding, layoutPadding, layoutPadding);

        if (mShowArrow) {
            mArrowView = new ImageView(mContext);
            mArrowView.setImageDrawable(mArrowDrawable);
            LinearLayout.LayoutParams arrowLayoutParams;

            if (mArrowDirection == ArrowDrawable.TOP || mArrowDirection == ArrowDrawable.BOTTOM) {
                arrowLayoutParams = new LinearLayout.LayoutParams((int) mArrowWidth, (int) mArrowHeight, 0);
            } else {
                arrowLayoutParams = new LinearLayout.LayoutParams((int) mArrowHeight, (int) mArrowWidth, 0);
            }

            arrowLayoutParams.gravity = Gravity.CENTER;
            mArrowView.setLayoutParams(arrowLayoutParams);

            if (mArrowDirection == ArrowDrawable.BOTTOM || mArrowDirection == ArrowDrawable.RIGHT) {
                linearLayout.addView(mContentView);
                linearLayout.addView(mArrowView);
            } else {
                linearLayout.addView(mArrowView);
                linearLayout.addView(mContentView);
            }
        } else {
            linearLayout.addView(mContentView);
        }

        LinearLayout.LayoutParams contentViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
        contentViewParams.gravity = Gravity.CENTER;
        mContentView.setLayoutParams(contentViewParams);

        if (mDismissOnInsideTouch || mDismissOnOutsideTouch)
            mContentView.setOnTouchListener(mPopupWindowTouchListener);

        mContentLayout = linearLayout;
        mContentLayout.setVisibility(View.INVISIBLE);
        mPopupWindow.setContentView(mContentLayout);
    }

    public void dismiss() {
        if (dismissed)
            return;

        dismissed = true;
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
    }

    /**
     * <div class="pt">Indica se o tooltip está sendo exibido na tela.</div>
     * <div class=en">Indicate whether this tooltip is showing on screen.</div>
     *
     * @return <div class="pt"><tt>true</tt> se o tooltip estiver sendo exibido, <tt>false</tt> caso
     * contrário</div>
     * <div class="en"><tt>true</tt> if the popup is showing, <tt>false</tt> otherwise</div>
     */
    public boolean isShowing() {
        return mPopupWindow != null && mPopupWindow.isShowing();
    }

    public <T extends View> T findViewById(int id) {
        //noinspection unchecked
        return (T) mContentLayout.findViewById(id);
    }

    @Override
    public void onDismiss() {
        dismissed = true;

        if (mAnimator != null) {
            mAnimator.removeAllListeners();
            mAnimator.end();
            mAnimator.cancel();
            mAnimator = null;
        }

        if (mRootView != null && mOverlay != null) {
            Log.v(TAG, "remove view from tooltip");
            mRootView.post(new Runnable() {
                @Override
                public void run() {
                    Log.v(TAG, "Will call remove view from tooltip");
                    try {
                        mRootView.removeView(mOverlay);
                    } catch (Exception e) {
                        Log.e(TAG, "", e);
                    }

                    mRootView = null;
                    mOverlay = null;
                    Log.v(TAG, "View has been removed");
                }
            });
        }

        if (mOnDismissListener != null)
            mOnDismissListener.onDismiss(this);
        mOnDismissListener = null;

        SimpleTooltipUtils.removeOnGlobalLayoutListener(mPopupWindow.getContentView(), mLocationLayoutListener);
        SimpleTooltipUtils.removeOnGlobalLayoutListener(mPopupWindow.getContentView(), mArrowLayoutListener);
        SimpleTooltipUtils.removeOnGlobalLayoutListener(mPopupWindow.getContentView(), mShowLayoutListener);
        SimpleTooltipUtils.removeOnGlobalLayoutListener(mPopupWindow.getContentView(), mAnimationLayoutListener);
        SimpleTooltipUtils.removeOnGlobalLayoutListener(mPopupWindow.getContentView(), mAutoDismissLayoutListener);

        mPopupWindow = null;
    }

    private final View.OnTouchListener mPopupWindowTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getX() > 0 && event.getX() < v.getWidth() &&
                    event.getY() > 0 && event.getY() < v.getHeight()) {
                if (mDismissOnInsideTouch) {
                    dismiss();
                    return mModal;
                }
                return false;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return mModal;
        }
    };

    private final View.OnTouchListener mOverlayTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mDismissOnOutsideTouch) {
                dismiss();
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return mModal;
        }
    };

    private final ViewTreeObserver.OnGlobalLayoutListener mLocationLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            final PopupWindow popup = mPopupWindow;
            if (popup == null || dismissed) return;

            if (mMaxWidth > 0 && mContentView.getWidth() > mMaxWidth) {
                SimpleTooltipUtils.setWidth(mContentView, mMaxWidth);
                popup.update(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                return;
            }

            SimpleTooltipUtils.removeOnGlobalLayoutListener(popup.getContentView(), this);
            popup.getContentView().getViewTreeObserver().addOnGlobalLayoutListener(mArrowLayoutListener);
            PointF location = calculePopupLocation();
            popup.setClippingEnabled(true);
            popup.update((int) location.x, (int) location.y, popup.getWidth(), popup.getHeight());
            popup.getContentView().requestLayout();
            createOverlay();
        }
    };

    private final ViewTreeObserver.OnGlobalLayoutListener mArrowLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            final PopupWindow popup = mPopupWindow;
            if (popup == null || dismissed) return;

            SimpleTooltipUtils.removeOnGlobalLayoutListener(popup.getContentView(), this);

            popup.getContentView().getViewTreeObserver().addOnGlobalLayoutListener(mAnimationLayoutListener);
            popup.getContentView().getViewTreeObserver().addOnGlobalLayoutListener(mShowLayoutListener);
            if (mShowArrow) {
                RectF achorRect = SimpleTooltipUtils.calculeRectOnScreen(mAnchorView);
                RectF contentViewRect = SimpleTooltipUtils.calculeRectOnScreen(mContentLayout);
                float x, y;
                if (mArrowDirection == ArrowDrawable.TOP || mArrowDirection == ArrowDrawable.BOTTOM) {
                    x = mContentLayout.getPaddingLeft() + SimpleTooltipUtils.pxFromDp(2);
                    float centerX = (contentViewRect.width() / 2f) - (mArrowView.getWidth() / 2f);
                    float newX = centerX - (contentViewRect.centerX() - achorRect.centerX());
                    if (newX > x) {
                        if (newX + mArrowView.getWidth() + x > contentViewRect.width()) {
                            x = contentViewRect.width() - mArrowView.getWidth() - x;
                        } else {
                            x = newX;
                        }
                    }
                    y = mArrowView.getTop();
                    y = y + (mArrowDirection == ArrowDrawable.BOTTOM ? -1 : +1);
                } else {
                    y = mContentLayout.getPaddingTop() + SimpleTooltipUtils.pxFromDp(2);
                    float centerY = (contentViewRect.height() / 2f) - (mArrowView.getHeight() / 2f);
                    float newY = centerY - (contentViewRect.centerY() - achorRect.centerY());
                    if (newY > y) {
                        if (newY + mArrowView.getHeight() + y > contentViewRect.height()) {
                            y = contentViewRect.height() - mArrowView.getHeight() - y;
                        } else {
                            y = newY;
                        }
                    }
                    x = mArrowView.getLeft();
                    x = x + (mArrowDirection == ArrowDrawable.RIGHT ? -1 : +1);
                }
                SimpleTooltipUtils.setX(mArrowView, (int) x);
                SimpleTooltipUtils.setY(mArrowView, (int) y);
            }
            popup.getContentView().requestLayout();
        }
    };

    private final ViewTreeObserver.OnGlobalLayoutListener mShowLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            final PopupWindow popup = mPopupWindow;
            if (popup == null || dismissed) return;

            SimpleTooltipUtils.removeOnGlobalLayoutListener(popup.getContentView(), this);

            if (mOnShowListener != null)
                mOnShowListener.onShow(SimpleTooltip.this);
            mOnShowListener = null;

            mContentLayout.setVisibility(View.VISIBLE);
        }
    };

    private final ViewTreeObserver.OnGlobalLayoutListener mAnimationLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            final PopupWindow popup = mPopupWindow;
            if (popup == null || dismissed) return;

            SimpleTooltipUtils.removeOnGlobalLayoutListener(popup.getContentView(), this);

            if (mAnimated) startAnimation();

            popup.getContentView().requestLayout();
        }
    };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startAnimation() {
        final String property = mGravity == Gravity.TOP || mGravity == Gravity.BOTTOM ? "translationY" : "translationX";

        final ObjectAnimator anim1 = ObjectAnimator.ofFloat(mContentLayout, property, -mAnimationPadding, mAnimationPadding);
        anim1.setDuration(mAnimationDuration);
        anim1.setInterpolator(new AccelerateDecelerateInterpolator());

        final ObjectAnimator anim2 = ObjectAnimator.ofFloat(mContentLayout, property, mAnimationPadding, -mAnimationPadding);
        anim2.setDuration(mAnimationDuration);
        anim2.setInterpolator(new AccelerateDecelerateInterpolator());

        mAnimator = new AnimatorSet();
        mAnimator.playSequentially(anim1, anim2);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!dismissed && isShowing()) {
                    animation.start();
                }
            }
        });
        mAnimator.start();
    }

    /**
     * <div class="pt">Listener utilizado para chamar o <tt>SimpleTooltip#dismiss()</tt> quando a
     * <tt>View</tt> root é encerrada sem que a tooltip seja fechada.
     * Pode ocorrer quando a tooltip é utilizada dentro de Dialogs.</div>
     */
    private final ViewTreeObserver.OnGlobalLayoutListener mAutoDismissLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            final PopupWindow popup = mPopupWindow;
            if (popup == null || dismissed) return;

            if (!mRootView.isShown()) dismiss();
        }
    };

    public interface OnDismissListener {
        void onDismiss(SimpleTooltip tooltip);
    }

    public interface OnShowListener {
        void onShow(SimpleTooltip tooltip);
    }

    /**
     * <div class="pt">Classe responsável por facilitar a criação do objeto
     * <tt>SimpleTooltip</tt>.</div>
     * <div class="en">Class responsible for making it easier to build the object
     * <tt>SimpleTooltip</tt>.</div>
     *
     * @author Created by douglas on 05/05/16.
     */
    @SuppressWarnings({"SameParameterValue", "unused"})
    public static class Builder {

        private final Context context;
        private boolean dismissOnInsideTouch = true;
        private boolean dismissOnOutsideTouch = true;
        private boolean modal = false;
        private View contentView;
        @IdRes
        private int textViewId = android.R.id.text1;
        private CharSequence text = "";
        private View anchorView;
        private int arrowDirection = ArrowDrawable.AUTO;
        private int gravity = Gravity.BOTTOM;
        private boolean transparentOverlay = true;
        private float overlayOffset = -1;
        private float maxWidth;
        private boolean showArrow = true;
        private Drawable arrowDrawable;
        private boolean animated = false;
        private float margin = -1;
        private float mNegativeMargin = 0;
        private float popWindowMargin = -1;
        private float padding = -1;
        private int topPadding = -1;
        private int bottomPadding = -1;
        private int leftPadding = -1;
        private int rightPadding = -1;
        private float animationPadding = -1;
        private OnDismissListener onDismissListener;
        private OnShowListener onShowListener;
        private long animationDuration;
        private int backgroundColor;
        private int textColor;
        private int arrowColor;
        private @ColorInt int arrowStrokeColor;
        private @Dimension int arrowStrokeWidth;
        private float arrowHeight;
        private float arrowWidth;
        private boolean focusable;
        private boolean keepInsideScreen;
        private int highlightShape = OverlayView.HIGHLIGHT_SHAPE_OVAL;

        public Builder(Context context) {
            this.context = context;
        }

        public SimpleTooltip build() throws IllegalArgumentException {
            validateArguments();
            if (backgroundColor == 0) {
                backgroundColor = SimpleTooltipUtils.getColor(context, DEFAULT_BACKGROUND_COLOR_RES);
            }
            if (textColor == 0) {
                textColor = SimpleTooltipUtils.getColor(context, DEFAULT_TEXT_COLOR_RES);
            }
            if (contentView == null) {
                TextView tv = new TextView(context);
                SimpleTooltipUtils.setTextAppearance(tv, DEFAULT_TEXT_APPEARANCE_RES);
                tv.setBackgroundColor(backgroundColor);
                tv.setTextColor(textColor);
                contentView = tv;
            }
            if (arrowColor == 0) {
                arrowColor = SimpleTooltipUtils.getColor(context, DEFAULT_ARROW_COLOR_RES);
            }
            arrowStrokeColor = SimpleTooltipUtils.getColor(context, R.color.grey_rebranding);
            arrowStrokeWidth = (int) context.getResources().getDimension(R.dimen.simpletooltip_arrow_stroke_width);

            if (margin < 0) {
                margin = context.getResources().getDimension(DEFAULT_MARGIN_RES);
            }
            if (popWindowMargin < 0) {
                popWindowMargin = context.getResources().getDimension(DEFAULT_POP_WINDOW_MARGIN_RES);
            }
            if (padding < 0) {
                padding = context.getResources().getDimension(DEFAULT_PADDING_RES);
            }
            if (animationPadding < 0) {
                animationPadding = context.getResources().getDimension(DEFAULT_ANIMATION_PADDING_RES);
            }
            if (animationDuration == 0) {
                animationDuration = context.getResources().getInteger(DEFAULT_ANIMATION_DURATION_RES);
            }
            if (showArrow) {
                if (arrowDirection == ArrowDrawable.AUTO)
                    arrowDirection = SimpleTooltipUtils.tooltipGravityToArrowDirection(gravity);
                if (arrowDrawable == null)
                    arrowDrawable = new ArrowDrawable(arrowColor, arrowDirection, arrowStrokeColor, arrowStrokeWidth);
                if (arrowWidth == 0)
                    arrowWidth = context.getResources().getDimension(DEFAULT_ARROW_WIDTH_RES);
                if (arrowHeight == 0)
                    arrowHeight = context.getResources().getDimension(DEFAULT_ARROW_HEIGHT_RES);
            }
            if (highlightShape < 0 || highlightShape > OverlayView.HIGHLIGHT_SHAPE_RECTANGULAR) {
                highlightShape = OverlayView.HIGHLIGHT_SHAPE_OVAL;
            }
            if (overlayOffset < 0) {
                overlayOffset = context.getResources().getDimension(DEFAULT_OVERLAY_OFFSET_RES);
            }
            return new SimpleTooltip(this);
        }

        private void validateArguments() throws IllegalArgumentException {
            if (context == null) {
                throw new IllegalArgumentException("Context not specified.");
            }
            if (anchorView == null) {
                throw new IllegalArgumentException("Anchor view not specified.");
            }
        }

        /**
         * <div class="pt">Define um novo conteúdo customizado para o tooltip.</div>
         *
         * @param textView <div class="pt">novo conteúdo para o tooltip.</div>
         * @return this
         * @see Builder#contentView(int, int)
         * @see Builder#contentView(View, int)
         * @see Builder#contentView(int)
         */
        public Builder contentView(TextView textView) {
            this.contentView = textView;
            this.textViewId = 0;
            return this;
        }

        /**
         * <div class="pt">Define um novo conteúdo customizado para o tooltip.</div>
         *
         * @param contentView <div class="pt">novo conteúdo para o tooltip, pode ser um <tt>{@link
         *                    ViewGroup}</tt> ou qualquer componente customizado.</div>
         * @param textViewId  <div class="pt">resId para o <tt>{@link TextView}</tt> existente
         *                    dentro do <tt>{@link Builder#contentView}</tt>. Padrão é
         *                    <tt>android.R.id.text1</tt>.</div>
         * @return this
         * @see Builder#contentView(int, int)
         * @see Builder#contentView(TextView)
         * @see Builder#contentView(int)
         */
        public Builder contentView(View contentView, @IdRes int textViewId) {
            this.contentView = contentView;
            this.textViewId = textViewId;
            return this;
        }

        /**
         * <div class="pt">Define um novo conteúdo customizado para o tooltip.</div>
         *
         * @param contentViewId <div class="pt">layoutId que será inflado como o novo conteúdo para
         *                      o tooltip.</div>
         * @param textViewId    <div class="pt">resId para o <tt>{@link TextView}</tt> existente
         *                      dentro do <tt>{@link Builder#contentView}</tt>. Padrão é
         *                      <tt>android.R.id.text1</tt>.</div>
         * @return this
         * @see Builder#contentView(View, int)
         * @see Builder#contentView(TextView)
         * @see Builder#contentView(int)
         */
        public Builder contentView(@LayoutRes int contentViewId, @IdRes int textViewId) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.contentView = inflater.inflate(contentViewId, null, false);
            this.textViewId = textViewId;
            return this;
        }

        /**
         * <div class="pt">Define um novo conteúdo customizado para o tooltip.</div>
         *
         * @param contentViewId <div class="pt">layoutId que será inflado como o novo conteúdo para
         *                      o tooltip.</div>
         * @return this
         * @see Builder#contentView(View, int)
         * @see Builder#contentView(TextView)
         * @see Builder#contentView(int, int)
         */
        public Builder contentView(@LayoutRes int contentViewId) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.contentView = inflater.inflate(contentViewId, null, false);
            this.textViewId = 0;
            return this;
        }

        /**
         * <div class="pt">Define se o tooltip será fechado quando receber um clique dentro de sua
         * área. Padrão é <tt>true</tt>.</div>
         *
         * @param dismissOnInsideTouch <div class="pt"><tt>true</tt> para fechar quando receber o
         *                             click dentro, <tt>false</tt> caso contrário.</div>
         * @return this
         * @see Builder#dismissOnOutsideTouch(boolean)
         */
        public Builder dismissOnInsideTouch(boolean dismissOnInsideTouch) {
            this.dismissOnInsideTouch = dismissOnInsideTouch;
            return this;
        }

        /**
         * <div class="pt">Define se o tooltip será fechado quando receber um clique fora de sua
         * área. Padrão é <tt>true</tt>.</div>
         *
         * @param dismissOnOutsideTouch <div class="pt"><tt>true</tt> para fechar quando receber o
         *                              click fora, <tt>false</tt> caso contrário.</div>
         * @return this
         * @see Builder#dismissOnInsideTouch(boolean)
         */
        public Builder dismissOnOutsideTouch(boolean dismissOnOutsideTouch) {
            this.dismissOnOutsideTouch = dismissOnOutsideTouch;
            return this;
        }

        /**
         * <div class="pt">Define se a tela fiacrá bloqueada enquanto o tooltip estiver aberto.
         * Esse parâmetro deve ser combinado com <tt>{@link Builder#dismissOnInsideTouch(boolean)}</tt>
         * e <tt>{@link Builder#dismissOnOutsideTouch(boolean)}</tt>.
         * Padrão é <tt>false</tt>.</div>
         *
         * @param modal <div class="pt"><tt>true</tt> para bloquear a tela, <tt>false</tt> caso
         *              contrário.</div>
         * @return this
         * @see Builder#dismissOnInsideTouch(boolean)
         * @see Builder#dismissOnOutsideTouch(boolean)
         */
        public Builder modal(boolean modal) {
            this.modal = modal;
            return this;
        }

        /**
         * <div class="pt">Define o texto que sera exibido no <tt>{@link TextView}</tt> dentro do
         * tooltip.</div>
         *
         * @param text <div class="pt">texto que sera exibido.</div>
         * @return this
         */
        public Builder text(CharSequence text) {
            this.text = text;
            return this;
        }

        /**
         * <div class="pt">Define o texto que sera exibido no <tt>{@link TextView}</tt> dentro do
         * tooltip.</div>
         *
         * @param textRes <div class="pt">id do resource da String.</div>
         * @return this
         */
        public Builder text(@StringRes int textRes) {
            this.text = context.getString(textRes);
            return this;
        }

        /**
         * <div class="pt">Define para qual <tt>{@link View}</tt> o tooltip deve apontar. Importante
         * ter certeza que esta <tt>{@link View}</tt> já esteja pronta e exibida na tela.</div>
         * <div class="en">Set the target <tt>{@link View}</tt> that the tooltip will point. Make
         * sure that the anchor <tt>{@link View}</tt> shold be showing in the screen.</div>
         *
         * @param anchorView <div class="pt"><tt>View</tt> para qual o tooltip deve apontar</div>
         *                   <div class="en"><tt>View</tt> that the tooltip will point</div>
         * @return this
         */
        public Builder anchorView(View anchorView) {
            this.anchorView = anchorView;
            return this;
        }

        /**
         * <div class="pt">Define a para qual lado o tooltip será posicionado em relação ao
         * <tt>anchorView</tt>.
         * As opções existentes são <tt>{@link Gravity#START}</tt>, <tt>{@link Gravity#END}</tt>,
         * <tt>{@link Gravity#TOP}</tt> e <tt>{@link Gravity#BOTTOM}</tt>.
         * O padrão é <tt>{@link Gravity#BOTTOM}</tt>.</div>
         *
         * @param gravity <div class="pt">lado para qual o tooltip será posicionado.</div>
         * @return this
         */
        public Builder gravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        /**
         * <div class="pt">Define a direção em que a seta será criada.
         * As opções existentes são <tt>{@link ArrowDrawable#LEFT}</tt>, <tt>{@link
         * ArrowDrawable#TOP}</tt>, <tt>{@link ArrowDrawable#RIGHT}</tt>,
         * <tt>{@link ArrowDrawable#BOTTOM}</tt> e <tt>{@link ArrowDrawable#AUTO}</tt>.
         * O padrão é <tt>{@link ArrowDrawable#AUTO}</tt>. <br>
         * Esta opção deve ser utilizada em conjunto com  <tt>Builder#showArrow(true)</tt>.</div>
         *
         * @param arrowDirection <div class="pt">direção em que a seta será criada.</div>
         * @return this
         */
        public Builder arrowDirection(int arrowDirection) {
            this.arrowDirection = arrowDirection;
            return this;
        }

        /**
         * <div class="pt">Define se o fundo da tela será escurecido ou transparente enquanto o
         * tooltip estiver aberto. Padrão é <tt>true</tt>.</div>
         *
         * @param transparentOverlay <div class="pt"><tt>true</tt> para o fundo transparente,
         *                           <tt>false</tt> para escurecido.</div>
         * @return this
         */
        public Builder transparentOverlay(boolean transparentOverlay) {
            this.transparentOverlay = transparentOverlay;
            return this;
        }

        /**
         * <div class="pt">Define a largura máxima do Tooltip.</div>
         *
         * @param maxWidthRes <div class="pt">resId da largura máxima.</div>
         * @return <tt>this</tt>
         * @see Builder#maxWidth(float)
         */
        public Builder maxWidth(@DimenRes int maxWidthRes) {
            this.maxWidth = context.getResources().getDimension(maxWidthRes);
            return this;
        }

        /**
         * <div class="pt">Define a largura máxima do Tooltip. Padrão é <tt>0</tt> (sem
         * limite).</div>
         *
         * @param maxWidth <div class="pt">largura máxima em pixels.</div>
         * @return <tt>this</tt>
         * @see Builder#maxWidth(int)
         */
        public Builder maxWidth(float maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        /**
         * <div class="pt">Define se o tooltip será animado enquanto estiver aberto. Disponível a
         * partir do Android API 11. Padrão é <tt>false</tt>.</div>
         *
         * @param animated <div class="pt"><tt>true</tt> para tooltip animado, <tt>false</tt> caso
         *                 contrário.</div>
         * @return this
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public Builder animated(boolean animated) {
            this.animated = animated;
            return this;
        }

        /**
         * @param animationPadding <div class="pt">tamanho do deslocamento em pixels.</div>
         * @return <tt>this</tt>
         * @see Builder#animationPadding(int)
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public Builder animationPadding(float animationPadding) {
            this.animationPadding = animationPadding;
            return this;
        }

        /**
         * @param animationPaddingRes <div class="pt">resId do tamanho do deslocamento.</div>
         * @return <tt>this</tt>
         * @see Builder#animationPadding(float)
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public Builder animationPadding(@DimenRes int animationPaddingRes) {
            this.animationPadding = context.getResources().getDimension(animationPaddingRes);
            return this;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public Builder animationDuration(long animationDuration) {
            this.animationDuration = animationDuration;
            return this;
        }

        /**
         * @param padding <div class="pt">tamanho do padding em pixels.</div>
         * @return <tt>this</tt>
         * @see Builder#padding(int)
         */
        public Builder padding(float padding) {
            this.padding = padding;
            return this;
        }

        public Builder setPadding(int left, int top, int right, int bottom) {
            this.leftPadding = left;
            this.topPadding = top;
            this.rightPadding = right;
            this.bottomPadding = bottom;
            return this;
        }

        /**
         * @param paddingRes <div class="pt">resId do tamanho do padding.</div>
         * @return <tt>this</tt>
         * @see Builder#padding(float)
         */
        public Builder padding(@DimenRes int paddingRes) {
            this.padding = context.getResources().getDimension(paddingRes);
            return this;
        }


        /**
         * @param margin <div class="pt">tamanho da margem em pixels.</div>
         * @return <tt>this</tt>
         * @see Builder#margin(int)
         */
        public Builder margin(float margin) {
            this.margin = margin;
            return this;
        }

        public Builder negativeMargin(@DimenRes int mNegativeMargin){
            this.mNegativeMargin = context.getResources().getDimension(mNegativeMargin);
            return this;
        }

        public Builder popWindowMargin(float margin) {
            this.popWindowMargin = margin;
            return this;
        }

        /**
         * @param marginRes <div class="pt">resId do tamanho da margem.</div>
         * @return <tt>this</tt>
         * @see Builder#margin(float)
         */
        public Builder margin(@DimenRes int marginRes) {
            this.margin = context.getResources().getDimension(marginRes);
            return this;
        }

        public Builder textColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder backgroundColor(@ColorInt int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        /**
         * <div class="pt">Indica se deve ser gerada a seta indicativa. Padrão é
         * <tt>true</tt>.</div>
         * <div class="en">Indicates whether to be generated indicative arrow. Default is
         * <tt>true</tt>.</div>
         *
         * @param showArrow <div class="pt"><tt>true</tt> para exibir a seta, <tt>false</tt> caso
         *                  contrário.</div>
         *                  <div class="en"><tt>true</tt> to show arrow, <tt>false</tt>
         *                  otherwise.</div>
         * @return this
         */
        public Builder showArrow(boolean showArrow) {
            this.showArrow = showArrow;
            return this;
        }

        public Builder arrowDrawable(Drawable arrowDrawable) {
            this.arrowDrawable = arrowDrawable;
            return this;
        }

        public Builder arrowDrawable(@DrawableRes int drawableRes) {
            this.arrowDrawable = SimpleTooltipUtils.getDrawable(context, drawableRes);
            return this;
        }

        public Builder arrowColor(@ColorInt int arrowColor) {
            this.arrowColor = arrowColor;
            return this;
        }

        /**
         * <div class="pt">Altura da seta indicativa. Esse valor é automaticamente definido em
         * Largura ou Altura conforme a <tt>{@link Gravity}</tt> configurada.
         * <div class="en">Height of the arrow. This values is automatically set in the Width or
         * Height as the <tt>{@link Gravity}</tt>.</div>
         *
         * @param arrowHeight <div class="pt">Altura em pixels.</div>
         *                    <div class="en">Height in pixels.</div>
         * @return this
         * @see Builder#arrowWidth(float)
         */
        public Builder arrowHeight(float arrowHeight) {
            this.arrowHeight = arrowHeight;
            return this;
        }

        /**
         * <div class="pt">Largura da seta indicativa. Esse valor é automaticamente definido em
         * Largura ou Altura conforme a <tt>{@link Gravity}</tt> configurada.
         * <div class="en">Width of the arrow. This values is automatically set in the Width or
         * Height as the <tt>{@link Gravity}</tt>.</div>
         *
         * @param arrowWidth <div class="pt">Largura em pixels.</div>
         *                   <div class="en">Width in pixels.</div>
         * @return this
         */
        public Builder arrowWidth(float arrowWidth) {
            this.arrowWidth = arrowWidth;
            return this;
        }

        public Builder onDismissListener(OnDismissListener onDismissListener) {
            this.onDismissListener = onDismissListener;
            return this;
        }

        public Builder onShowListener(OnShowListener onShowListener) {
            this.onShowListener = onShowListener;
            return this;
        }

        /**
         * <div class="pt">Habilita o foco no conteúdo da tooltip. Padrão é <tt>false</tt>.</div>
         * <div class="en">Enables focus in the tooltip content. Default is <tt>false</tt>.</div>
         *
         * @param focusable <div class="pt">Pode receber o foco.</div>
         *                  <div class="en">Can receive focus.</div>
         * @return this
         */
        public Builder focusable(boolean focusable) {
            this.focusable = focusable;
            return this;
        }

        /**
         * <div class="pt">Configura o formato do Shape em destaque. <br/>
         * <tt>{@link OverlayView#HIGHLIGHT_SHAPE_OVAL}</tt> - Destaque oval (padrão). <br/>
         * <tt>{@link OverlayView#HIGHLIGHT_SHAPE_RECTANGULAR}</tt> - Destaque retangular. <br/>
         * </div>
         * <p>
         * <div class="en">Configure the the Shape type. <br/>
         * <tt>{@link OverlayView#HIGHLIGHT_SHAPE_OVAL}</tt> - Shape oval (default). <br/>
         * <tt>{@link OverlayView#HIGHLIGHT_SHAPE_RECTANGULAR}</tt> - Shape rectangular. <br/>
         * </div>
         *
         * @param highlightShape <div class="pt">Formato do Shape.</div>
         *                       <div class="en">Shape type.</div>
         * @return this
         * @see OverlayView#HIGHLIGHT_SHAPE_OVAL
         * @see OverlayView#HIGHLIGHT_SHAPE_RECTANGULAR
         * @see Builder#transparentOverlay(boolean)
         */
        public Builder highlightShape(int highlightShape) {
            this.highlightShape = highlightShape;
            return this;
        }

        /**
         * <div class="pt">Tamanho da margem entre {@link Builder#anchorView(View)} e a borda do
         * Shape de destaque.
         * <div class="en">Margin between {@link Builder#anchorView(View)} and highlight Shape
         * border.
         *
         * @param overlayOffset <div class="pt">Tamanho em pixels.</div>
         *                      <div class="en">Size in pixels.</div>
         * @return this
         * @see Builder#anchorView(View)
         * @see Builder#transparentOverlay(boolean)
         */
        public Builder overlayOffset(@Dimension float overlayOffset) {
            this.overlayOffset = overlayOffset;
            return this;
        }

        public Builder keepInsideScreen(boolean keepInsideScreen) {
            this.keepInsideScreen = keepInsideScreen;
            return this;
        }
    }
}