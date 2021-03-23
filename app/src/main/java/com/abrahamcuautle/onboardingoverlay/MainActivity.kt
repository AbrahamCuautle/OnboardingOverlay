package com.abrahamcuautle.onboardingoverlay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        OnboardingOverlay.Builder(this)
            .setBackgroundColor(R.color.design_default_color_primary)
            .setMode(OnboardingOverlay.Mode.CIRCLE)
            .build()

    }
}