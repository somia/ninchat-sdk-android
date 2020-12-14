package com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.presenter

class OnChangeListener(
        private val intervalInMs: Long = 250,
        private val callback: (text: CharSequence?) -> Unit,
) {
    var lastClickedTimeInMs = 0L
    fun onChange(text: CharSequence?) {
        val now = System.currentTimeMillis()
        if (now - lastClickedTimeInMs >= intervalInMs) {
            lastClickedTimeInMs = now
            callback(text)
        }
    }
}