package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter

class OnToggleListener(
        private val intervalInMs: Long = 1000,
) {
    var lastClickedTimeInMs = 0L
    fun onButtonToggle(callback: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastClickedTimeInMs >= intervalInMs) {
            lastClickedTimeInMs = now
            callback()
        }
    }
}