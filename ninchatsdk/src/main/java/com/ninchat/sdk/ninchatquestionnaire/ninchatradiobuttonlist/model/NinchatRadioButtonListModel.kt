package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.model

import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter
import org.json.JSONArray
import org.json.JSONObject

data class NinchatRadioButtonListModel(
        val isFormLikeQuestionnaire: Boolean = false,
        var label: String? = "",
        var hasError: Boolean? = false,
        var value: String? = "",
        var optionList: JSONArray? = null,
        var fireEvent: Boolean = false,
) {

    fun parse(jsonObject: JSONObject?) {
        this.label = NinchatQuestionnaireItemGetter.getLabel(jsonObject)
        this.value = NinchatQuestionnaireItemGetter.getResultString(jsonObject)
        this.hasError = NinchatQuestionnaireItemGetter.getError(jsonObject)
        this.fireEvent = jsonObject?.optBoolean("fireEvent", false) ?: false
        this.optionList = NinchatQuestionnaireItemGetter.getOptions(jsonObject)
    }

    fun update(jsonObject: JSONObject) {
        this.hasError = NinchatQuestionnaireItemGetter.getError(jsonObject)
        this.value = NinchatQuestionnaireItemGetter.getResultString(jsonObject)
    }

    fun getIndex(value: String?): Int {
        return this.optionList?.let {
            for (i in 0 until it.length()) {
                if (NinchatQuestionnaireItemGetter.getLabel(it.optJSONObject(i)) == value) {
                    return i
                }
            }
            -1
        } ?: -1
    }
}