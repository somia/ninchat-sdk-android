package com.ninchat.sdk.ninchatchatactivity.view

import android.content.res.Configuration
import android.view.View
import android.view.ViewTreeObserver

class SoftKeyboardViewHandler(
    private val onHidden: () -> Unit,
    private val onShow: () -> Unit,
) {
    private var previousHeight = -1
    private var rootView: View? = null
    private var wasOpen = false

    fun register(rootView: View) {
        this.rootView = rootView
        rootView.viewTreeObserver.addOnGlobalLayoutListener(observer)
    }

    fun unregister() {
        rootView?.viewTreeObserver?.removeOnGlobalLayoutListener(observer)
    }

    private val observer = ViewTreeObserver.OnGlobalLayoutListener {
        val currentRootView = rootView ?: return@OnGlobalLayoutListener
        val height = currentRootView.height
        val heightDifference = height - previousHeight
        val currentOrientation = currentRootView.resources.configuration.orientation
        if (heightDifference > 0 && currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            // soft keyboard is hidden
            if (wasOpen) {
                onHidden()
                wasOpen = false
            }

        } else if (heightDifference < 0 && currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            // softkeyboard is shown
            if (!wasOpen) {
                onShow()
                wasOpen = true
            }
        }
        if (previousHeight != height) {
            previousHeight = height
        }
    }
}
