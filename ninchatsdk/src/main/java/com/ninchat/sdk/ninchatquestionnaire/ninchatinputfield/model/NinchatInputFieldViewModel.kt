package com.ninchat.sdk.ninchatquestionnaire.ninchatinputfield.model

import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter
import org.json.JSONObject

data class NinchatInputFieldViewModel(
        val isMultiline: Boolean,
        var pattern: String? = "",
        var isFormLikeQuestionnaire: Boolean,
        var label: String? = "",
        var value: String? = "",
        var inputType: Int = 0,
        var hasError: Boolean = false,
        var hasFocus: Boolean = false,
) {
    fun parse(jsonObject: JSONObject?): NinchatInputFieldViewModel {
        this.label = NinchatQuestionnaireItemGetter.getLabel(jsonObject)
        this.value = NinchatQuestionnaireItemGetter.getResultString(jsonObject)
        this.hasError = NinchatQuestionnaireItemGetter.getError(jsonObject)
        this.inputType = NinchatQuestionnaireItemGetter.getInputType(jsonObject)
        this.pattern = NinchatQuestionnaireItemGetter.getPattern(jsonObject)
        return this
    }
}