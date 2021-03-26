package com.abrahamcuautle.onboardingoverlay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btn).setOnClickListener {
            Log.d("TAG_APP", "Post")
            OnboardingOverlay.Builder(this)
                .setBackgroundColor(android.R.color.black)
                .setMode(OnboardingOverlay.Mode.RECTANGLE)
                .build()
                .show(findViewById(R.id.btn))
        }



    }
}