package com.ninchat.sdk.ninchatquestionnaire.ninchattextviewholder.model

import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter
import org.json.JSONObject

data class NinchatTextViewModel(var label: String? = "", val isFormLikeQuestionnaire: Boolean = true) {
    fun parse(jsonObject: JSONObject?) {
        this.label = NinchatQuestionnaireItemGetter.getLabel(jsonObject)
    }
}