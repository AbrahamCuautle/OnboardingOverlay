package com.abrahamcuautle.onboardingoverlay;

import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.Nullable;

public class DisplayUtils {
    private DisplayUtils() {

    }

    public static int getWidthScreen(@Nullable WindowManager windowManager){
        if (windowManager == null){
            return 0;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static int getHeightScreen(@Nullable WindowManager windowManager){
        if (windowManager == null){
            return 0;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }
}
