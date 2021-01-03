package com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.presenter

class OnClickListener(
        private val intervalInMs: Long = 2000,
        private val onBack: () -> Unit,
        private val onNext: () -> Unit,
) {
    var lastClickedTimeInMs = 0L
    fun onBackButtonClicked() {
        val now = System.currentTimeMillis()
        if (now - lastClickedTimeInMs >= intervalInMs) {
            lastClickedTimeInMs = now
            onBack()
        }
    }
    fun onNextButtonClicked() {
        val now = System.currentTimeMillis()
        if (now - lastClickedTimeInMs >= intervalInMs) {
            lastClickedTimeInMs = now
            onNext()
        }
    }
}