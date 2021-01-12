package com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.presenter

class OnChangeListener(
        private val intervalInMs: Long = 250,
) {
    var lastClickedTimeInMs = 0L
    fun onChange(callback: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastClickedTimeInMs >= intervalInMs) {
            lastClickedTimeInMs = now
            callback()
        }
    }
}