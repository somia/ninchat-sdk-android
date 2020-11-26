package com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.model

import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil
import org.json.JSONObject

data class NinchatButtonViewModel(
        var showPreviousImageButton: Boolean = false,
        var showPreviousTextButton: Boolean = false,
        var showNextImageButton: Boolean = false,
        var showNextTextButton: Boolean = false,

        var previousButtonLabel: String? = "",
        var nextButtonLabel: String? = "",

        var previousButtonClicked: Boolean = false,
        var nextButtonClicked: Boolean = false,

        var fireEvent: Boolean = false,
        var isThankYouText: Boolean = false,
) {

    fun parse(jsonObject: JSONObject?) {
        val hasPreviousButton = NinchatQuestionnaireMiscUtil.hasButton(jsonObject, true)
        val previousButtonLabel = jsonObject?.optString("back", "") ?: ""
        val hasNextButton = NinchatQuestionnaireMiscUtil.hasButton(jsonObject, false)
        val nextButtonLabel = jsonObject?.optString("next", "") ?: ""
        val showBackImageButton = hasPreviousButton && (previousButtonLabel == "true" || previousButtonLabel.isEmpty())
        val showNextImageButton = hasNextButton && (nextButtonLabel == "true" || nextButtonLabel.isEmpty())
        val fireEvent = jsonObject?.optBoolean("fireEvent", false) ?: false
        val isThankYouText = jsonObject?.optString("type", "") == "thankYouText"


        this.showPreviousImageButton = showBackImageButton
        this.showNextImageButton = showNextImageButton

        this.showPreviousTextButton = hasPreviousButton && !showBackImageButton
        this.showNextTextButton = hasNextButton && !showNextImageButton

        this.previousButtonLabel = previousButtonLabel
        this.nextButtonLabel = nextButtonLabel
        this.fireEvent = fireEvent
        this.isThankYouText = isThankYouText
        // do necessary translation
        this.translate()
    }

    private fun translate() {
        // translate back and next button label
        previousButtonLabel = previousButtonLabel?.let {
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getTranslation(it)
                    ?: previousButtonLabel
        } ?: previousButtonLabel

        nextButtonLabel = nextButtonLabel?.let {
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getTranslation(it)
                    ?: nextButtonLabel
        } ?: nextButtonLabel
    }
}