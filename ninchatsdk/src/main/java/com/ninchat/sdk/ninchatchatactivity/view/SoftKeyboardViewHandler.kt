package com.ninchat.sdk.ninchatchatactivity.view

import android.content.res.Configuration
import android.view.View

class SoftKeyboardViewHandler(
    private val onHidden: () -> Unit,
    private val onShow: () -> Unit,
    private val onHeightChange: (currentHeight: Int) -> Unit,
) {
    private var previousHeight = -1
    private lateinit var rootView: View

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
            onHidden()
        } else if (heightDifference < 0 && currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            // softkeyboard is shown
            onShow()
        }
        if (previousHeight != height) {
            previousHeight = height
        }
        onHeightChange(height)
    }
}
