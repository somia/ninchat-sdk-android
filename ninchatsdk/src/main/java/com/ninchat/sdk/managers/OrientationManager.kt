package com.ninchat.sdk.managers

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.OrientationEventListener

class OrientationManager(private val activity: Activity, rate: Int) : OrientationEventListener(activity, rate) {
    private var oldRotation = -1
    override fun onOrientationChanged(orientation: Int) {
        if (orientation < 0) return
        var curOrientation = oldRotation
        when {
            orientation <= 45 -> curOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            orientation <= 135 -> {
                // pass
            }
            orientation <= 225 -> {
                // pass
            }
            orientation <= 315 -> curOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        if (oldRotation != curOrientation) {
            oldRotation = curOrientation
            changeOrientation(curOrientation)
        }
    }

    private fun changeOrientation(orientation: Int) {
        when (orientation) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }
}