package com.ninchat.sdk.ninchatchatactivity.view

import android.content.res.Configuration
import android.view.View

class SoftKeyboardViewHandler(
    private val onHidden: () -> Unit,
    private val onShow: () -> Unit,
) {
    private var previousHeight = -1
    private lateinit var rootView: View
    private var wasOpen = false

    fun register(rootView: View) {
        this.rootView = rootView
        rootView.viewTreeObserver.addOnGlobalLayoutListener(observer)
    }

    fun unregister() {
        rootView.viewTreeObserver.removeOnGlobalLayoutListener(observer)
    }

    private val observer = {
        val height = rootView.height
        val heightDifference = height - previousHeight
        val currentOrientation = rootView.resources.configuration.orientation
        if (heightDifference > 0 && currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            // soft keyboard is hidden
            if(wasOpen) {
                onHidden()
                wasOpen = false
            }

        } else if (heightDifference < 0 && currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            // softkeyboard is shown
            if(!wasOpen) {
                onShow()
                wasOpen = true
            }
        }
        if (previousHeight != height) {
            previousHeight = height
        }
    }
}
