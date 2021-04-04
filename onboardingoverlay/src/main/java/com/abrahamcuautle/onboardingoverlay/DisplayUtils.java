package com.abrahamcuautle.onboardingoverlay;

import android.graphics.Point;
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
        Point point = new Point();
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealSize(point);
        return point.x;
    }

    public static int getHeightScreen(@Nullable WindowManager windowManager){
        if (windowManager == null){
            return 0;
        }
        Point point = new Point();
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealSize(point);
        return point.y;
    }
}
