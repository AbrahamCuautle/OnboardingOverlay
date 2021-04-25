# Onboarding Overlay

<img src="https://img.shields.io/badge/status-development-brightgreen"/>

A showcase library to introduce users to a new feature

![Screenshot](https://github.com/AbrahamCuautle/OnboardingOverlay/blob/main/screenshots/demo-overlay.gif)

## Installation

Add this in your root `build.gradle` file:
```gradle
allprojects {
    repositories {
        maven { url "https://www.jitpack.io" }
    }
}
```

Then, add the library to your module `build.gradle`:
```gradle
dependencies {
    implementation 'com.github.AbrahamCuautle:OnboardingOverlay:{latest_version}'
}
```

## Usage
```java
OnboardingOverlay.Builder(context)
                    //Set overlay background color. By default is black with 85% of transparency
                    //if you set a solid background color, alpha color's channel will be 85% 
                    //but if you use a background color with transparecy, alpha color's channel
                    // will not be modified
                    .setBackgroundColor(R.color.design_default_color_primary_variant)
                    // The shape you want to be displayed over your view
                    .setMode(OnboardingOverlay.Mode.RECTANGLE)
                    .setTitle("What's an onboarding?")
                    .setDescription("An onboarding experience is a way to introduce users to a new product, app, or feature.")
                    .setTextButton("Got it!")
                    .build()
                    //Then, show it!
                    .show(your_view)
```

__Note:__ The view you take as reference  ```show```, must be laid out otherwise library will throw an exception. 
In case you need to set up this library when your Activity or Fragment has been created (```onCreate()``` or ```onViewCreated()``` respectively),
you could use [View KTX extensions](https://developer.android.com/reference/kotlin/androidx/core/view/package-summary#doonpredraw) (such as ```View.doOnPreDraw()```) to avoid the exception mentioned before.

Additionally, you can style the text and button by passing a style resource:

```xml
    <style name="OnboardingTitleStyle" parent="Widget.MaterialComponents.TextView">
        <item name="android:textSize">20sp</item>
        <item name="android:textColor">@android:color/white</item>
        <item name="android:fontFamily">@font/inter_medium</item>
    </style>

    <style name="OnboardingDescriptionStyle" parent="Widget.MaterialComponents.TextView">
        <item name="android:textSize">14sp</item>
        <item name="android:textColor">@android:color/white</item>
        <item name="android:fontFamily">@font/inter_regular</item>
    </style>

    <style name="OnboardingButtonStyle" parent="Widget.MaterialComponents.Button.TextButton">
        <item name="android:textSize">12sp</item>
        <item name="android:textColor">@android:color/white</item>
        <item name="android:fontFamily">@font/inter_semi_bold</item>
        <item name="rippleColor">@android:color/white</item>
    </style>

    <style name="OnboardingOverlayStyle">
        <item name="onboardingTitleStyle">@style/OnboardingTitleStyle</item>
        <item name="onboardingDescriptionStyle">@style/OnboardingDescriptionStyle</item>
        <item name="onboardingButtonStyle">@style/OnboardingButtonStyle</item>
    </style>
    
```

```java
OnboardingOverlay.Builder(context, R.style.OnboardingOverlayStyle)
```
