package com.syntax.core.utils

import android.animation.ArgbEvaluator

object ColorUtils {
    private val argbEvaluator = ArgbEvaluator()

    /**
     * Interpolates between two colors based on the given fraction.
     *
     * @param startColor The starting color (e.g., green).
     * @param endColor The ending color (e.g., red).
     * @param fraction The fraction of interpolation (0.0 to 1.0).
     * @return The interpolated color.
     */
    fun interpolateColor(startColor: Int, endColor: Int, fraction: Float): Int {
        return argbEvaluator.evaluate(fraction, startColor, endColor) as Int
    }
}