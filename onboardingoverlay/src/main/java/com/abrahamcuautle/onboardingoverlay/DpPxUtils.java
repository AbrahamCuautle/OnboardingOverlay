package com.abrahamcuautle.onboardingoverlay;

import android.content.res.Resources;

public class DpPxUtils {

    private DpPxUtils() { }

    public static float pxToDp(int px) {
        return px * Resources.getSystem().getDisplayMetrics().density + 0.5f;
    }

}
