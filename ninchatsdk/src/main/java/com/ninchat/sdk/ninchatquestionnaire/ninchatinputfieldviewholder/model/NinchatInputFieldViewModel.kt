package com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.model

import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireConstants
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
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
        val position: Int,
        var enabled: Boolean
) {
    fun parse(jsonObject: JSONObject?) {
        this.label = jsonObject?.optString(NinchatQuestionnaireConstants.label)
        this.value = jsonObject?.optString(NinchatQuestionnaireConstants.result)
        this.hasError = jsonObject?.optBoolean(NinchatQuestionnaireConstants.hasError) ?: false
        this.inputType = NinchatQuestionnaireJsonUtil.getInputType(jsonObject)
        this.pattern = jsonObject?.optString(NinchatQuestionnaireConstants.pattern)
    }

    fun update(jsonObject: JSONObject, enabled: Boolean) {
        this.enabled = enabled
        this.hasError = jsonObject.optBoolean(NinchatQuestionnaireConstants.hasError)
        this.value = jsonObject.optString(NinchatQuestionnaireConstants.result)
    }
}