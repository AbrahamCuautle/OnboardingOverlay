package com.abrahamcuautle.onboardingoverlay;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewKt;

import com.google.android.material.button.MaterialButton;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class OnboardingOverlay {

    private WindowManager mWindowManager;

    private final int mBackgroundColor;

    private final Context mContext;

    private View mOverlayView;

    private View mReferenceView;

    private int mReferenceViewX;

    private int mReferenceViewY;

    private int mMode;

    private int mGravityContent;

    private boolean mIsShowing;

    @IntDef({Mode.RECTANGLE, Mode.CIRCLE})
    @Retention(RetentionPolicy.SOURCE)
    @interface Mode {
        int RECTANGLE = 0;
        int CIRCLE = 1;
    }

    private OnboardingOverlay(Builder builder) {
        this.mBackgroundColor = builder.mBackgroundColor;
        this.mMode = builder.mMode;
        this.mContext = builder.context.get();
        this.mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
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
            mWindowManager.removeView(mOverlayView);
        }
    }

    public static class Builder {

        private WeakReference<Context> context;

        private int mBackgroundColor;

        @Mode
        private int mMode;

        public Builder(@NonNull Context context) {
            this.context = new WeakReference<>(context);
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
                    : ColorUtils.setAlphaComponent(color, 0xA6); //0x80 means 50% transparency

            return this;
        }

        public Builder setMode(@Mode int mode) {
            this.mMode = mode;
            return this;
        }

        public OnboardingOverlay build()  {
            return new OnboardingOverlay(this);
        }

    }

    private class OverLayView extends FrameLayout {

        /*private TextView titleTextView;

        private TextView descriptionTextView;

        private MaterialButton button;*/

        public OverLayView(@NonNull Context context) {
            super(context);
            addView(new BackgroundOverlayView(context));
            LinearLayout ll = createContainer();
            generateLayoutParams(ll);
            addView(ll);
        }

        private void generateLayoutParams(LinearLayout ll){
            if(mReferenceView != null && !ViewCompat.isAttachedToWindow(mReferenceView)) {
                return ;
            }

            int leftMargin = (int) DpPxUtils.pxToDp(30);
            int rightMargin = (int) DpPxUtils.pxToDp(30);

            int width = DisplayUtils.getWidthScreen(mWindowManager) - leftMargin - rightMargin;

            LayoutParams lp = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
            ll.setLayoutParams(lp);

            int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.UNSPECIFIED);
            int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            measureChild(ll, widthSpec, heightSpec);


        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            startOpenCircleReveal();
        }

        private void startOpenCircleReveal() {
            if(mReferenceView != null && ViewCompat.isAttachedToWindow(mReferenceView)){

                int heightScreen = DisplayUtils.getHeightScreen(mWindowManager);

                float cx = (float) (mReferenceViewX +  (mReferenceView.getWidth() / 2));
                float cy = (float) (mReferenceViewY + (mReferenceView.getHeight() / 2));

                Animator animator = ViewAnimationUtils.createCircularReveal(mOverlayView, (int) cx, (int) cy, 0, heightScreen);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(500L);
                animator.start();
            }
        }

        private void startCloseCircleReveal() {
            if(mReferenceView != null && ViewCompat.isAttachedToWindow(mReferenceView)){

                int height = DisplayUtils.getHeightScreen(mWindowManager);

                float cx = (float) (mReferenceViewX +  (mReferenceView.getWidth() / 2));
                float cy = (float) (mReferenceViewY + (mReferenceView.getHeight() / 2));

                Animator animator = ViewAnimationUtils.createCircularReveal(mOverlayView, (int) cx, (int) cy, height, 0);
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
            LinearLayout container = new LinearLayout(getContext());
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
            lpbtn.topMargin = (int) DpPxUtils.pxToDp(8);
            lpbtn.rightMargin = (int) DpPxUtils.pxToDp(15);
            lpbtn.gravity = Gravity.END;
            container.addView(createButton(), lpbtn);

            return container;
        }

        private TextView createTitleTextView() {
            TextView titleTextView = new TextView(getContext());
            titleTextView.setTextSize(24);
            titleTextView.setTextColor(Color.WHITE);
            titleTextView.setText("Onboarding definition.");
            return titleTextView;
        }

        private TextView createDescriptionTextView() {
            TextView descriptionTextView = new TextView(getContext());
            descriptionTextView.setTextSize(18);
            descriptionTextView.setTextColor(Color.WHITE);
            descriptionTextView.setText("An onboarding experience is a way to introduce users to a new product, app, or feature.");
            return descriptionTextView;
        }

        private MaterialButton createButton() {
            MaterialButton button = new MaterialButton(
                    getContext(), null, R.attr.materialButtonOutlinedStyle);
            button.setTextSize(14);
            button.setTextColor(Color.WHITE);
            button.setText("Got it!");
            button.setStrokeWidth(0);
            button.setRippleColor(ColorStateList.valueOf(Color.WHITE));
            button.setOnClickListener(v -> { startCloseCircleReveal(); });
            return button;
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
