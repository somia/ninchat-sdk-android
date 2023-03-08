package com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter

import android.util.Log

import android.view.View
import android.view.ViewTreeObserver

class LayoutChangeListener(
    onConfigurationChange: (newWidth: Int, newHeight: Int) -> Unit
) {
    private lateinit var rootView: View
    var prevWidth = -1
    var prevHeight = -1

    fun register(view: View) {
        rootView = view
        rootView.viewTreeObserver.addOnGlobalLayoutListener(observer)
    }

    fun unregister() {
        rootView.viewTreeObserver.removeOnGlobalLayoutListener(observer)
    }

    private val observer = ViewTreeObserver.OnGlobalLayoutListener {
        val height = rootView.height
        val width = rootView.width
        if (prevHeight != height || prevWidth != width) {
            prevHeight = height
            prevWidth = width
            onConfigurationChange(width, height)
        }
    }
}