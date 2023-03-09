package com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter


import android.view.View
import android.view.ViewTreeObserver

class LayoutChangeListener(
    onConfigurationChange: (newWidth: Int, newHeight: Int) -> Unit
) {
    var rootView: View ? = null
    var prevWidth = -1
    var prevHeight = -1

    fun register(view: View) {
        rootView = view
        rootView?.viewTreeObserver?.addOnGlobalLayoutListener(observer)
    }

    fun unregister() {
        rootView?.viewTreeObserver?.removeOnGlobalLayoutListener(observer)
    }

    private val observer = ViewTreeObserver.OnGlobalLayoutListener {
        val height = rootView?.height ?: 0
        val width = rootView?.width ?: 0
        if (prevHeight != height || prevWidth != width) {
            prevHeight = height
            prevWidth = width
            onConfigurationChange(width, height)
        }
    }
}