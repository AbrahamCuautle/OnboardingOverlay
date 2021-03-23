package com.abrahamcuautle.onboardingoverlay;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.ColorRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

public class OnboardingOverlay {

    private WindowManager mWindowManager;

    private View mView;

    private boolean mIsShowing;

    @IntDef({Mode.RECTANGLE, Mode.CIRCLE})
    @Retention(RetentionPolicy.SOURCE)
    @interface Mode {
        int RECTANGLE = 0;
        int CIRCLE = 1;
    }

    private OnboardingOverlay(Builder builder) {

    }

    public void show(View view) {
        this.mView = view;

        if (!ViewCompat.isAttachedToWindow(view)) {
            throw new IllegalStateException(view + "is not attached");
        }

        if (view.getWidth() == 0 || view.getHeight() == 0) {
            throw new  IllegalStateException("View's width and height must be greater than 0");
        }

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        mWindowManager.addView(view /* Show Overlayview */, lp);
    }

    public void dimiss() {
        if (mView.isAttachedToWindow()) {
            mWindowManager.removeView(mView);
        }
    }

    public static class Builder {

        private WeakReference<Context> context;

        private int backgroundColor;

        public Builder(Context context) {
            this.context = new WeakReference<>(context);
        }

        public Builder setBackgroundColor(@ColorRes int colorRes) {
            Context context = this.context.get();
            if (context == null){
                return this;
            }
            int color = ContextCompat.getColor(context, colorRes);
            this.backgroundColor = ColorUtils.setAlphaComponent(color, 0x80);

            return this;
        }

        public int getBackgroundColor() {
            return backgroundColor;
        }

        public Builder setEnterTransition(int enter) {
            return this;
        }

        public Builder setExitTransition(int exit) {
            return this;
        }

        public Builder setMode(@Mode int mode) {
            return this;
        }

        public OnboardingOverlay build()  {
            return new OnboardingOverlay(this);
        }

    }

    private class OverLayView extends FrameLayout {

        public OverLayView(@NonNull Context context) {
            super(context);
        }


    }

}
