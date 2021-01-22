package com.ninchat.sdk.ninchatquestionnaire.ninchatcheckboxlist.model

import com.ninchat.sdk.ninchatquestionnaire.helper.fromJSONArray
import org.json.JSONObject

data class NinchatCheckBoxListModel(
        val isFormLikeQuestionnaire: Boolean = true,
        var optionList: List<JSONObject> = listOf(),
        val position: Int,
        var enabled: Boolean,
) {

    fun parse(jsonObject: JSONObject?) {
        this.optionList = fromJSONArray<JSONObject>(questionnaireList = jsonObject?.optJSONArray("options"))
                .map { it as JSONObject }
    }

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        this.enabled = enabled
        this.optionList = fromJSONArray<JSONObject>(questionnaireList = jsonObject?.optJSONArray("options"))
                .map { it as JSONObject }
    }
}