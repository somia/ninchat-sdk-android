package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.model

import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireConstants
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
        this.label = jsonObject?.optString(NinchatQuestionnaireConstants.label)
        this.value = jsonObject?.optString(NinchatQuestionnaireConstants.result)
        this.position = jsonObject?.optInt(NinchatQuestionnaireConstants.position) ?: -1
        this.hasError = jsonObject?.optBoolean(NinchatQuestionnaireConstants.hasError) ?: false
        this.fireEvent = jsonObject?.optBoolean(NinchatQuestionnaireConstants.fireEvent) ?: false
        this.optionList = jsonObject?.optJSONArray(NinchatQuestionnaireConstants.options)
                ?: JSONArray()
    }

    fun update(jsonObject: JSONObject) {
        this.hasError = jsonObject.optBoolean(NinchatQuestionnaireConstants.hasError, false)
        this.value = jsonObject.optString(NinchatQuestionnaireConstants.result)
        this.position = jsonObject.optInt(NinchatQuestionnaireConstants.position, -1)
    }

    fun getValue(position: Int): String? {
        return optionList?.let {
            if (position >= it.length()) return null
            it.optJSONObject(position)?.optString(NinchatQuestionnaireConstants.value)
        }
    }

    @Deprecated("will be removed once converted to kotlin data model")
    fun updateJson(jsonObject: JSONObject?) {
        jsonObject?.putOpt(NinchatQuestionnaireConstants.result, this.value)
        jsonObject?.putOpt(NinchatQuestionnaireConstants.position, this.position)
        jsonObject?.putOpt(NinchatQuestionnaireConstants.hasError, this.hasError)
    }
}