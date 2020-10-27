package com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.model

import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemSetter
import org.json.JSONObject

data class NinchatCheckboxViewModel(
        var isFormLikeQuestionnaire: Boolean,
        var isChecked: Boolean = false,
        var label: String? = "",
        var hasError: Boolean = false,
        var fireEvent: Boolean = false,
) {

    fun parse(jsonObject: JSONObject?) {
        this.isChecked = NinchatQuestionnaireItemGetter.getResultBoolean(jsonObject)
        this.label = NinchatQuestionnaireItemGetter.getLabel(jsonObject)
        this.hasError = NinchatQuestionnaireItemGetter.getError(jsonObject)
        this.fireEvent = jsonObject?.optBoolean("fireEvent", false) ?: false
        // may be translate
        this.translate()
    }

    fun update(jsonObject: JSONObject?) {
        this.isChecked = NinchatQuestionnaireItemGetter.getResultBoolean(jsonObject)
        this.hasError = NinchatQuestionnaireItemGetter.getError(jsonObject)
    }

    private fun translate() {
        // translate back and next button label
        label = label?.let {
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getTranslation(it)
                    ?: label
        } ?: label
    }


    @Deprecated("will be removed once converted to kotlin data model")
    fun updateJson(jsonObject: JSONObject?) {
        NinchatQuestionnaireItemSetter.setResult(jsonObject, this.isChecked)
        NinchatQuestionnaireItemSetter.setError(jsonObject, this.hasError)
    }
}