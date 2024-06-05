package com.example.simplewebdavmanager.utils

import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Object to help with UI animations
 */
object UIUtil {
    /**
     * Show the back button
     */
    fun showBackButton(button: FloatingActionButton) {
        button.apply {
            visibility = View.VISIBLE
            animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(300)
                .start()
        }
    }

    /**
     * Hide the back button
     */
    fun hideBackButton(button: FloatingActionButton) {
        button.apply {
            animate()
                .translationY(height.toFloat())
                .alpha(0f)
                .setDuration(300)
                .withEndAction { visibility = View.GONE }
                .start()
        }
    }
}
