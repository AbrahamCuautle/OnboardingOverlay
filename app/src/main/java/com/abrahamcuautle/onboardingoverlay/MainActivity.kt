package com.abrahamcuautle.onboardingoverlay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btn).setOnClickListener {
            OnboardingOverlay.Builder(this)
                .setBackgroundColor(android.R.color.black)
                .setMode(OnboardingOverlay.Mode.CIRCLE)
                .setTitle("What's an onboarding?")
                .setDescription("An onboarding experience is a way to introduce users to a new product, app, or feature.")
                .setTextButton("Got it!")
                .build()
                .show(findViewById(R.id.btn))
        }

        findViewById<View>(R.id.btn1).setOnClickListener {
            OnboardingOverlay.Builder(this)
                    .setBackgroundColor(R.color.design_default_color_secondary_variant)
                    .setMode(OnboardingOverlay.Mode.CIRCLE)
                    .setTitle("What's an onboarding?")
                    .setDescription("An onboarding experience is a way to introduce users to a new product, app, or feature.")
                    .setTextButton("Got it!")
                        .build()
                    .show(findViewById(R.id.btn1))
        }

        findViewById<View>(R.id.btn2).setOnClickListener {
            OnboardingOverlay.Builder(this)
                    .setBackgroundColor(R.color.purple_200)
                    .setMode(OnboardingOverlay.Mode.CIRCLE)
                    .setTitle("What's an onboarding?")
                    .setDescription("An onboarding experience is a way to introduce users to a new product, app, or feature.")
                    .setTextButton("Got it!")
                    .build()
                    .show(findViewById(R.id.btn2))
        }

        findViewById<View>(R.id.btn3).setOnClickListener {
            OnboardingOverlay.Builder(this)
                    .setBackgroundColor(R.color.design_default_color_primary_variant)
                    .setMode(OnboardingOverlay.Mode.CIRCLE)
                    .setTitle("What's an onboarding?")
                    .setDescription("An onboarding experience is a way to introduce users to a new product, app, or feature.")
                    .setTextButton("Got it!")
                    .build()
                    .show(findViewById(R.id.btn3))
        }

        findViewById<View>(R.id.btn4).setOnClickListener {
            OnboardingOverlay.Builder(this)
                .setBackgroundColor(android.R.color.holo_red_light)
                .setMode(OnboardingOverlay.Mode.CIRCLE)
                .setTitle("What's an onboarding?")
                .setDescription("An onboarding experience is a way to introduce users to a new product, app, or feature.")
                .setTextButton("Got it!")
                .build()
                .show(findViewById(R.id.btn4))
        }

    }
}