package com.ninchat.sdk.ninchatquestionnaire.ninchathyperlink.presenter

class OnClickListener(
        private val intervalInMs: Long = 2000,
) {
    var lastClickedTimeInMs = 0L
    fun onClickListener(callback: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastClickedTimeInMs >= intervalInMs) {
            lastClickedTimeInMs = now
            callback()
        }
    }
}