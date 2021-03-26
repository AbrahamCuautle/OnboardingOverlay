package com.abrahamcuautle.onboardingoverlay;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;

import com.google.android.material.button.MaterialButton;

import org.w3c.dom.Text;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

public class
OnboardingOverlay {

    private WindowManager mWindowManager;

    private final int mBackgroundColor;

    private final Context mContext;

    private View mOverlayView;

    private View mReferenceView;

    private int mMode;

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

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                0,
                PixelFormat.TRANSLUCENT);

        mOverlayView = new OverLayView(mContext);

        if (mWindowManager != null) {
            mWindowManager.addView(mOverlayView, lp);
        }
    }

    public void dismiss() {
        if (mWindowManager != null
                && mOverlayView != null
                &&mOverlayView.isAttachedToWindow()) {
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
            this.mBackgroundColor = ColorUtils.setAlphaComponent(color, 0x80);

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
            addView(createContainer());
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
            lptvd.topMargin = 15;
            container.addView(createDescriptionTextView(), lptvd);

            //Add button
            LinearLayout.LayoutParams lpbtn = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lpbtn.topMargin = 15;
            container.addView(createButton(), lpbtn);

            return container;
        }

        private TextView createTitleTextView() {
            TextView titleTextView = new TextView(getContext());
            titleTextView.setTextSize(24);
            titleTextView.setTextColor(Color.WHITE);
            titleTextView.setText("Title Onboarding");
            return titleTextView;
        }

        private TextView createDescriptionTextView() {
            TextView descriptionTextView = new TextView(getContext());
            descriptionTextView.setTextSize(18);
            descriptionTextView.setTextColor(Color.WHITE);
            descriptionTextView.setText("Description Onboarding");
            return descriptionTextView;
        }

        private MaterialButton createButton() {
            MaterialButton button = new MaterialButton(new ContextThemeWrapper(
                    getContext(), R.style.Widget_MaterialComponents_Button_OutlinedButton));
            button.setTextSize(14);
            button.setTextColor(Color.WHITE);
            button.setText("Got it!");
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
                        dismiss();
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

        private final Paint mPaintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);

        private final Paint mPaintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);

        private float mRadius;

        private float cx;

        private float cy;

        private ValueAnimator valueAnimator;

        public BackgroundOverlayView(Context context) {
            super(context);

            mPaintCircle.setColor(Color.RED);
            mPaintCircle.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

            mPaintBackground.setColor(mBackgroundColor);

            if(mReferenceView != null && ViewCompat.isAttachedToWindow(mReferenceView)){
                mRadius = (float) Math.max(mReferenceView.getWidth(), mReferenceView.getHeight()) / 2;
                mRadius += 8; //Add Extra Spacing

                int[] location = new int[2];
                mReferenceView.getLocationOnScreen(location);
                cx = (float) (location[0] +  (mReferenceView.getWidth() / 2));
                cy = (float) (location[1] + (mReferenceView.getHeight() / 2));
            }

            setUpCircleAnimator();

        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Log.d("TAG_APP", "Canvas");
            if (valueAnimator != null && !valueAnimator.isRunning()) {
                valueAnimator.start();
            }
            canvas.drawRect(0, 0, getWidth(), getHeight(), mPaintBackground);
            canvas.drawCircle(cx, cy, mRadius, mPaintCircle);
        }


        private void setUpCircleAnimator() {
            valueAnimator = ValueAnimator.ofFloat(mRadius, mRadius + 15);
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
