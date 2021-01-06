package com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.model

import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireConstants
import org.json.JSONObject

data class NinchatCheckboxViewModel(
        var isFormLikeQuestionnaire: Boolean,
        var isChecked: Boolean = false,
        var label: String? = "",
        var hasError: Boolean = false,
        var fireEvent: Boolean = false,
        val position: Int,
        var enabled: Boolean
) {

    fun parse(jsonObject: JSONObject?) {
        this.isChecked = jsonObject?.optBoolean(NinchatQuestionnaireConstants.result) ?: false
        this.label = jsonObject?.optString(NinchatQuestionnaireConstants.label)
        this.label = this.label?.let {
            if (jsonObject?.optBoolean("required", false) == true) "$it *" else it
        }
        this.hasError = jsonObject?.optBoolean(NinchatQuestionnaireConstants.hasError) ?: false
        this.fireEvent = jsonObject?.optBoolean(NinchatQuestionnaireConstants.fireEvent) ?: false
        // may be translate
        this.translate()
    }

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        this.enabled = enabled
        this.isChecked = jsonObject?.optBoolean("result", false)?: false
        this.hasError = jsonObject?.optBoolean("hasError", false)?: false
    }

    private fun translate() {
        // translate back and next button label
        label = label?.let {
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getTranslation(it)
                    ?: label
        } ?: label
    }

}