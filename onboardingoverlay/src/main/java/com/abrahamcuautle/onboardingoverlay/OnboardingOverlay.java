package com.abrahamcuautle.onboardingoverlay;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewKt;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.Timer;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class OnboardingOverlay {

    private WindowManager mWindowManager;

    private int mBackgroundColor;

    private final Context mContext;

    private View mOverlayView;

    private View mReferenceView;

    private int mReferenceViewX;

    private int mReferenceViewY;

    private int mMode;

    @StyleRes
    private int mStyle;

    private String mTextTitle;

    private String mTextDescription;

    private String mTextButton;

    private boolean mIsShowing;

    private OnDismissListener onDismissListener;

    @IntDef({Mode.RECTANGLE, Mode.CIRCLE})
    @Retention(RetentionPolicy.SOURCE)
    @interface Mode {
        int RECTANGLE = 0;
        int CIRCLE = 1;
    }

    interface OnDismissListener {
        void onDismiss();
    }

    private OnboardingOverlay(Builder builder) {
        this.mContext = builder.context.get();
        this.mBackgroundColor = builder.mBackgroundColor == -1
                ? ColorUtils.setAlphaComponent(ContextCompat.getColor(mContext, android.R.color.black), 0xCC)
                : builder.mBackgroundColor;
        this.mMode = builder.mMode;
        this.mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        this.mStyle = builder.mStyle;
        this.mTextTitle = builder.textTitle;
        this.mTextDescription = builder.textDescription;
        this.mTextButton = builder.textButton;
    }

    public void show(@NonNull View view) {
        if (!ViewCompat.isAttachedToWindow(view)) {
            throw new IllegalStateException(view + "is not attached");
        }

        if (view.getWidth() == 0 || view.getHeight() == 0) {
            throw new  IllegalStateException("View's width and height must be greater than 0");
        }

        this.mReferenceView = view;
        computeXAndYReferenceView();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR,
                PixelFormat.TRANSLUCENT);

        mOverlayView = new OverLayView(mContext);

        if (mWindowManager != null) {
            mIsShowing = true;
            mWindowManager.addView(mOverlayView, lp);
        }
    }

    private void computeXAndYReferenceView() {
        int[] location = new int[2];
        mReferenceView.getLocationInWindow(location);
        mReferenceViewX = location[0];
        mReferenceViewY = location[1];
    }

    public void dismiss() {
        if (mWindowManager != null
                && mOverlayView != null
                && mOverlayView.isAttachedToWindow()) {
            mIsShowing = false;
            mWindowManager.removeView(mOverlayView);
            if (onDismissListener != null){
                onDismissListener.onDismiss();
            }
        }
    }

    private boolean isShowing() {
        return mIsShowing;
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    public static class Builder {

        private WeakReference<Context> context;

        private int mBackgroundColor = -1;

        @Mode
        private int mMode;

        @StyleRes
        private int mStyle;

        private String textTitle;

        private String textDescription;

        private String textButton;

        public Builder(@NonNull Context context) {
            this.context = new WeakReference<>(context);
        }

        public Builder(@NonNull Context context, @StyleRes int styleRes) {
            this.context = new WeakReference<>(context);
            this.mStyle = styleRes;
        }

        public Builder setBackgroundColor(@ColorRes int colorRes) {
            Context context = this.context.get();
            if (context == null){
                return this;
            }
            int color = ContextCompat.getColor(context, colorRes);
            int alpha = color & 0xFF000000;
            this.mBackgroundColor = alpha == 0xFF
                    ? color
                    : ColorUtils.setAlphaComponent(color, 0xCC); //0xCC means 80% transparency

            return this;
        }

        public Builder setMode(@Mode int mode) {
            this.mMode = mode;
            return this;
        }

        public Builder setTitle(String title) {
            this.textTitle = title;
            return this;
        }

        public Builder setDescription(String description) {
            this.textDescription = description;
            return this;
        }

        public Builder setTextButton(String button) {
            this.textButton = button;
            return this;
        }

        public OnboardingOverlay build()  {
            return new OnboardingOverlay(this);
        }

    }

    private class OverLayView extends FrameLayout {

        public OverLayView(@NonNull Context context) {
            super(context);
            addView(new BackgroundOverlayView(context));
            LinearLayout ll = createContainer();
            //ll.setBackgroundColor(Color.BLUE);
            generateLayoutParams(ll);
            addView(ll);
        }

        private void generateLayoutParams(LinearLayout ll){
            if(mReferenceView != null && !ViewCompat.isAttachedToWindow(mReferenceView)) {
                return ;
            }

            int leftMargin = (int) DpPxUtils.pxToDp(30);
            int rightMargin = (int) DpPxUtils.pxToDp(30);

            int widthScreen = DisplayUtils.getWidthScreen(mWindowManager);
            int heightScreen = DisplayUtils.getHeightScreen(mWindowManager);

            int widthContent =  Math.min(widthScreen, heightScreen) - leftMargin - rightMargin;

            LayoutParams lp = new LayoutParams(widthContent, LayoutParams.WRAP_CONTENT);
            ll.setLayoutParams(lp);

            int widthSpec = MeasureSpec.makeMeasureSpec(widthContent, MeasureSpec.EXACTLY);
            int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            measureChild(ll, widthSpec, heightSpec);

            //Space Available acordding to mReferenceView position
            int leftSpaceAvailable = mReferenceViewX;
            int topSpaceAvailable = mReferenceViewY;
            int rightSpaceAvailable = widthScreen - (mReferenceViewX + mReferenceView.getWidth());
            int bottomSpaceAvailable = heightScreen - (mReferenceViewY + mReferenceView.getHeight());

            int maxXAxis = Math.max(topSpaceAvailable, bottomSpaceAvailable);
            int maxYAxis = Math.max(rightSpaceAvailable, leftSpaceAvailable);
            int maxSpace = Math.max(maxXAxis, maxYAxis);

            if (maxSpace == topSpaceAvailable) {
                //More space available on top of mReferenceView
                lp.gravity = Gravity.BOTTOM;
                lp.bottomMargin = getBottomMargin();

                lp.leftMargin = leftMargin;
                lp.rightMargin = rightMargin;
            } else if (maxSpace == bottomSpaceAvailable) {
                //More space available on bottom of mReferenceView
                lp.gravity = Gravity.TOP;
                lp.topMargin = getTopMargin();

                lp.leftMargin = leftMargin;
                lp.rightMargin = rightMargin;
            } else if (maxSpace == leftSpaceAvailable) {
                lp.rightMargin = getRightMargin();
                lp.width = adjustWidthAccordingToLeftSpaceAvailable(leftSpaceAvailable, widthContent);

                if (topSpaceAvailable > bottomSpaceAvailable) {
                    lp.gravity = Gravity.END | Gravity.BOTTOM;
                    lp.bottomMargin = heightScreen
                            - mReferenceViewY
                            - mReferenceView.getHeight()
                            - getMissingBottomMargin(topSpaceAvailable, ll);

                } else {
                    lp.gravity = Gravity.END | Gravity.TOP;
                    lp.topMargin = mReferenceViewY
                            + mReferenceView.getHeight()
                            + getMissingTopMargin(bottomSpaceAvailable, ll);
                }

            } else {
                lp.leftMargin = getLeftMargin();
                lp.width = adjustWidthAccordingToRightSpaceAvailable(rightSpaceAvailable, widthContent);

                if (topSpaceAvailable > bottomSpaceAvailable) {
                    lp.gravity = Gravity.START | Gravity.BOTTOM;
                    lp.bottomMargin = heightScreen
                            - mReferenceViewY
                            - mReferenceView.getHeight()
                            - getMissingBottomMargin(topSpaceAvailable, ll);
                } else {
                    lp.gravity = Gravity.START | Gravity.TOP;
                    lp.topMargin = mReferenceViewY
                            + mReferenceView.getHeight()
                            + getMissingTopMargin(bottomSpaceAvailable, ll);
                }
            }

            ll.requestLayout();

        }

        private int adjustWidthAccordingToLeftSpaceAvailable(int leftSpaceAvailable, int widthContent) {
            return leftSpaceAvailable > widthContent
                    ? widthContent
                    : leftSpaceAvailable - (int) DpPxUtils.pxToDp(30); //15dp for rightMargin & 15dp for leftMargin
        }

        private int adjustWidthAccordingToRightSpaceAvailable(int rightSpaceAvailable, int widthContent) {
            return rightSpaceAvailable > widthContent
                    ? widthContent
                    : rightSpaceAvailable - (int) DpPxUtils.pxToDp(30); //15dp for rightMargin & 15dp for leftMargin
        }

        private int getMissingTopMargin(int bottomSpaceAvailable, @NotNull LinearLayout content) {
            return bottomSpaceAvailable > content.getMeasuredHeight()
                    ? 0
                    : bottomSpaceAvailable - content.getMeasuredHeight();
        }

        private int getMissingBottomMargin(int topSpaceAvailable, @NotNull LinearLayout content) {
            return topSpaceAvailable > content.getMeasuredHeight()
                    ? 0
                    : topSpaceAvailable - content.getMeasuredHeight();
        }

        private int getRightMargin() {
            int rightMargin = 0;
            int widthScreen = DisplayUtils.getWidthScreen(mWindowManager);
            switch (mMode) {
                case Mode.CIRCLE:
                    double mRadius = Math.hypot(mReferenceView.getWidth(), mReferenceView.getHeight()) / 2;
                    rightMargin = widthScreen
                            - mReferenceViewX - mReferenceView.getWidth() / 2
                            + (int) mRadius
                            + (int) DpPxUtils.pxToDp(4) //extra spacing
                            + (int) DpPxUtils.pxToDp(8); //end radius for animation;
                    break;
                case Mode.RECTANGLE:
                    rightMargin = widthScreen
                            - mReferenceViewX
                            + (int) DpPxUtils.pxToDp(5) //extra spacing
                            + (int) DpPxUtils.pxToDp(8); //end radius for animation;
                    break;
            }
            return rightMargin;
        }

        private int getLeftMargin() {
            int leftMargin = 0;
            switch (mMode) {
                case Mode.CIRCLE:
                    double mRadius = Math.hypot(mReferenceView.getWidth(), mReferenceView.getHeight()) / 2;
                    leftMargin = mReferenceViewX
                            + mReferenceView.getWidth() / 2
                            + (int) mRadius
                            + (int) DpPxUtils.pxToDp(4)
                            + (int) DpPxUtils.pxToDp(8);
                    break;
                case Mode.RECTANGLE:
                    leftMargin = mReferenceViewX
                            + mReferenceView.getWidth()
                            + (int) DpPxUtils.pxToDp(5) //extra spacing
                            + (int) DpPxUtils.pxToDp(8); //end radius for animation;
                    break;
            }
            return leftMargin;
        }

        private int getBottomMargin() {
            int bottomMargin = 0;
            int heightScreen = DisplayUtils.getHeightScreen(mWindowManager);
            switch (mMode) {
                case Mode.CIRCLE:
                    double mRadius = Math.hypot(mReferenceView.getWidth(), mReferenceView.getHeight()) / 2;
                    bottomMargin = heightScreen
                            - mReferenceViewY - mReferenceView.getHeight() / 2
                            + (int) mRadius
                            + (int) DpPxUtils.pxToDp(4)
                            + (int) DpPxUtils.pxToDp(8);
                    break;
                case Mode.RECTANGLE:
                    bottomMargin = heightScreen
                            - mReferenceViewY
                            + (int) DpPxUtils.pxToDp(5) //extra spacing
                            + (int) DpPxUtils.pxToDp(8); //end radius for animation;;
                    break;
            }
            return bottomMargin;
        }

        private int getTopMargin() {
            int topMargin = 0;
            switch (mMode){
                case Mode.CIRCLE:
                    double mRadius = Math.hypot(mReferenceView.getWidth(), mReferenceView.getHeight()) / 2;
                    topMargin = mReferenceViewY
                            + (mReferenceView.getHeight() / 2)
                            + (int) Math.round(mRadius)
                            + (int) DpPxUtils.pxToDp(4) //extra space
                            + (int) DpPxUtils.pxToDp(8); //end radius for animation
                    break;
                case Mode.RECTANGLE:
                    topMargin = mReferenceViewY
                            + mReferenceView.getHeight()
                            + (int) DpPxUtils.pxToDp(5) //extra spacing
                            + (int) DpPxUtils.pxToDp(8); //end radius for animation
                    break;
            }
            return topMargin;
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            startOpenCircleReveal();
        }

        private void startOpenCircleReveal() {
            if(mReferenceView != null && ViewCompat.isAttachedToWindow(mReferenceView)){

                int heightScreen = DisplayUtils.getHeightScreen(mWindowManager);
                int widthScreen = DisplayUtils.getWidthScreen(mWindowManager);
                int endRadius = Math.max(heightScreen, widthScreen);

                float cx = (float) (mReferenceViewX +  (mReferenceView.getWidth() / 2));
                float cy = (float) (mReferenceViewY + (mReferenceView.getHeight() / 2));

                Animator animator = ViewAnimationUtils.createCircularReveal(mOverlayView, (int) cx, (int) cy, 0, endRadius);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(500L);
                animator.start();
            }
        }

        private void startCloseCircleReveal() {
            if(mReferenceView != null && ViewCompat.isAttachedToWindow(mReferenceView)){

                int heightScreen = DisplayUtils.getHeightScreen(mWindowManager);
                int widthScreen = DisplayUtils.getWidthScreen(mWindowManager);
                int startRadius = Math.max(heightScreen, widthScreen);

                float cx = (float) (mReferenceViewX +  (mReferenceView.getWidth() / 2));
                float cy = (float) (mReferenceViewY + (mReferenceView.getHeight() / 2));

                Animator animator = ViewAnimationUtils.createCircularReveal(mOverlayView, (int) cx, (int) cy, startRadius, 0);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        setVisibility(GONE);
                        dismiss();
                    }
                });
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(500L);
                animator.start();
            }
        }

        private LinearLayout createContainer() {
            LinearLayout container = new LinearLayout(mContext);
            container.setOrientation(LinearLayout.VERTICAL);

            //Add Title TextView
            LinearLayout.LayoutParams lptvt = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            container.addView(createTitleTextView(), lptvt);

            //Add Description TextView
            LinearLayout.LayoutParams lptvd = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lptvd.topMargin = (int) DpPxUtils.pxToDp(8);
            container.addView(createDescriptionTextView(), lptvd);

            //Add button
            LinearLayout.LayoutParams lpbtn = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lpbtn.rightMargin = (int) DpPxUtils.pxToDp(15);
            lpbtn.gravity = Gravity.END;
            container.addView(createButton(), lpbtn);

            return container;
        }

        @NotNull
        private TextView createTitleTextView() {
            TextView titleTextView = new TextView(resolveTheme(), null, R.attr.onboardingTitleStyle);
            titleTextView.setText(mTextTitle);
            return titleTextView;
        }

        @NotNull
        private TextView createDescriptionTextView() {
            TextView descriptionTextView = new TextView(resolveTheme(),null, R.attr.onboardingDescriptionStyle);
            descriptionTextView.setText(mTextDescription);
            return descriptionTextView;
        }

        @NotNull
        private MaterialButton createButton() {
            MaterialButton button = new MaterialButton(resolveTheme(), null, R.attr.onboardingButtonStyle);
            button.setText(mTextButton);
            button.setOnClickListener(v -> startCloseCircleReveal());
            return button;
        }

        @NotNull
        private Context resolveTheme(){
            return mStyle == 0
                    ? new ContextThemeWrapper(getContext(), R.style.OnboardingOverlayStyle)
                    : new ContextThemeWrapper(getContext(), mStyle);
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if (getKeyDispatcherState() == null) {
                    return super.dispatchKeyEvent(event);
                }

                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                    final KeyEvent.DispatcherState state = getKeyDispatcherState();
                    if (state != null) {
                        state.startTracking(event, this);
                    }
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    final KeyEvent.DispatcherState state = getKeyDispatcherState();
                    if (state != null && state.isTracking(event) && !event.isCanceled()) {
                        startCloseCircleReveal();
                        return true;
                    }
                }
                return super.dispatchKeyEvent(event);
            } else {
                return super.dispatchKeyEvent(event);
            }
        }

    }

    private class BackgroundOverlayView extends View {

        private final Paint mPaintReference = new Paint(Paint.ANTI_ALIAS_FLAG);

        private final Paint mPaintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);

        private float mRadius;

        private float cx;

        private float cy;

        private float mRectSide;

        private final float mRectSpacing = DpPxUtils.pxToDp(5);

        private final float mCornerRadius = DpPxUtils.pxToDp(8);

        private ValueAnimator valueAnimator;

        public BackgroundOverlayView(Context context) {
            super(context);

            mPaintReference.setColor(Color.RED);
            mPaintReference.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

            mPaintBackground.setColor(mBackgroundColor);

            switch (mMode) {
                case Mode.CIRCLE:
                    if(mReferenceView != null && ViewCompat.isAttachedToWindow(mReferenceView)){
                        mRadius = (float)  Math.hypot(mReferenceView.getWidth(), mReferenceView.getHeight()) / 2;
                        mRadius += DpPxUtils.pxToDp(4); //Add Extra Spacing

                        cx = (float) (mReferenceViewX +  (mReferenceView.getWidth() / 2));
                        cy = (float) (mReferenceViewY + (mReferenceView.getHeight() / 2));
                    }
                    setUpCircleAnimator();
                    break;
                case Mode.RECTANGLE:
                    setUpRoundRectAnimator();
                    break;
            }

        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (valueAnimator != null && !valueAnimator.isRunning()) {
                valueAnimator.start();
            }
            canvas.drawRect(0, 0, getWidth(), getHeight(), mPaintBackground);
            switch (mMode) {
                case Mode.CIRCLE:
                    canvas.drawCircle(cx, cy, mRadius, mPaintReference);
                    break;
                case Mode.RECTANGLE:
                    if(mReferenceView != null && ViewCompat.isAttachedToWindow(mReferenceView)){
                        canvas.drawRoundRect(
                               mReferenceViewX - mRectSpacing - mRectSide,
                               mReferenceViewY - mRectSpacing - mRectSide,
                               mReferenceViewX + mReferenceView.getWidth() + mRectSpacing + mRectSide,
                                mReferenceViewY + mReferenceView.getHeight() + mRectSpacing + mRectSide,
                                mCornerRadius,
                                mCornerRadius,
                                mPaintReference
                        );
                    }
                    break;
            }
        }

        private void setUpRoundRectAnimator() {
            float extra = DpPxUtils.pxToDp(8);
            valueAnimator = ValueAnimator.ofFloat(0f, extra);
            valueAnimator.addUpdateListener(animation -> {
                mRectSide = (float) animation.getAnimatedValue();
                invalidate();
            });
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
            valueAnimator.setDuration(500L);
        }

        private void setUpCircleAnimator() {
            float extra = DpPxUtils.pxToDp(8);
            valueAnimator = ValueAnimator.ofFloat(mRadius, mRadius + extra);
            valueAnimator.addUpdateListener(animation -> {
                mRadius = (float) animation.getAnimatedValue();
                invalidate();
            });
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
            valueAnimator.setDuration(500L);
        }

    }

}
