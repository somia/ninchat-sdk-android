package com.ninchat.sdk.ninchatquestionnaire.model

data class NinchatButtonViewModel(
        var showPreviousImageButton: Boolean = false,
        var showPreviousTextButton: Boolean = false,
        var showNextImageButton: Boolean = false,
        var showNextTextButton: Boolean = false,

        var previousButtonLabel: String = "",
        var nextButtonLabel: String = "",

        var backButtonEnabled: Boolean = false,
        var nextButtonEnabled: Boolean = false
) {
    private val defaultBackButtonLabel: String = "Back"
    private val defaultNextButtonLabel: String = "Next"

    init {
        previousButtonLabel = if (previousButtonLabel.isNullOrEmpty()) defaultBackButtonLabel else previousButtonLabel
        nextButtonLabel = if (nextButtonLabel.isNullOrEmpty()) defaultNextButtonLabel else nextButtonLabel
    }

}