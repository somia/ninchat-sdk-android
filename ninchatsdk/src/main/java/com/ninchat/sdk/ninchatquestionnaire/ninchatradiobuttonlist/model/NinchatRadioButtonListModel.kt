package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.model

import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemSetter
import org.json.JSONArray
import org.json.JSONObject

data class NinchatRadioButtonListModel(
        val isFormLikeQuestionnaire: Boolean = true,
        var label: String? = "",
        var hasError: Boolean = false,
        var value: String? = "",
        var optionList: JSONArray? = null,
        var position: Int = -1,
        var fireEvent: Boolean = false,
) {

    fun parse(jsonObject: JSONObject?) {
        this.label = NinchatQuestionnaireItemGetter.getLabel(jsonObject)
        this.value = NinchatQuestionnaireItemGetter.getResultString(jsonObject)
        this.position = NinchatQuestionnaireItemGetter.getOptionPosition(jsonObject)
        this.hasError = NinchatQuestionnaireItemGetter.getError(jsonObject)
        this.fireEvent = jsonObject?.optBoolean("fireEvent", false) ?: false
        this.optionList = NinchatQuestionnaireItemGetter.getOptions(jsonObject)
    }

    fun update(jsonObject: JSONObject) {
        this.hasError = NinchatQuestionnaireItemGetter.getError(jsonObject)
        this.value = NinchatQuestionnaireItemGetter.getResultString(jsonObject)
        this.position = NinchatQuestionnaireItemGetter.getOptionPosition(jsonObject)
    }

    fun getValue(position: Int): String? {
        return optionList?.let {
            if(position >= it.length()) return null
            NinchatQuestionnaireItemGetter.getValue(it.optJSONObject(position))
        }
    }

    @Deprecated("will be removed once converted to kotlin data model")
    fun updateJson(jsonObject: JSONObject?) {
        NinchatQuestionnaireItemSetter.setResult(jsonObject, this.value)
        NinchatQuestionnaireItemSetter.setPosition(jsonObject, this.position)
        NinchatQuestionnaireItemSetter.setError(jsonObject, this.hasError)
    }
}