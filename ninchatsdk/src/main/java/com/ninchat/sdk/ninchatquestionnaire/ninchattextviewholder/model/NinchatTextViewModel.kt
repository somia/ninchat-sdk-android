package com.ninchat.sdk.ninchatquestionnaire.ninchattextviewholder.model

import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireConstants
import org.json.JSONObject

data class NinchatTextViewModel(var label: String? = "", val isFormLikeQuestionnaire: Boolean = true, val position: Int) {
    fun parse(jsonObject: JSONObject?) {
        this.label = jsonObject?.optString(NinchatQuestionnaireConstants.label)
    }
}