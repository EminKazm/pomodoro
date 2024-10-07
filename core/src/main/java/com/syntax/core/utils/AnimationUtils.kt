package com.syntax.core.utils

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation

object AnimationUtils {
    /**
     * Applies a pulsing animation to the given view.
     *
     * @param view The view to animate.
     */
    fun applyPulsingAnimation(view: View) {
        val alphaAnimation = AlphaAnimation(1.0f, 0.8f).apply {
            duration = 1000
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE
        }
        view.startAnimation(alphaAnimation)
    }

    /**
     * Stops any animation on the given view.
     *
     * @param view The view to stop animating.
     */
    fun stopAnimation(view: View) {
        view.clearAnimation()
    }
}
