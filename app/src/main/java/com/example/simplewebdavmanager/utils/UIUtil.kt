package com.example.simplewebdavmanager.utils

import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton

object UIUtil {
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
