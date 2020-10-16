package com.ninchat.sdk.ninchatquestionnaire.model

import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter
import org.json.JSONObject

data class NinchatCheckboxViewModel(
        var isFormLikeQuestionnaire: Boolean = true,
        var isChecked: Boolean = false,
        var label: String? = "",
        var hasError: Boolean = false,

        ) {
    fun parse(jsonObject: JSONObject?): NinchatCheckboxViewModel {
        this.isChecked = NinchatQuestionnaireItemGetter.getResultBoolean(jsonObject)
        this.label = NinchatQuestionnaireItemGetter.getLabel(jsonObject)
        this.hasError = NinchatQuestionnaireItemGetter.getError(jsonObject)
        // may be translate
        this.translate()
        return this
    }

    private fun translate() {
        // translate back and next button label
        label = label?.let {
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getTranslation(it)
                    ?: label
        } ?: label
    }
}