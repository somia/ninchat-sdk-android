package com.ninchat.sdk.ninchatquestionnaire.model

data class NinchatButtonViewModel(
        val showPreviousImageButton: Boolean = false,
        val showPreviousTextButton: Boolean = false,
        val showNextImageButton: Boolean = false,
        val showNextTextButton: Boolean = false,

        val nextButtonLabel: String = "",
        val previousButtonLabel: String = "",
)