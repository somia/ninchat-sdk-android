package com.ninchat.sdk.ninchatquestionnaire.model

import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter
import org.json.JSONObject

data class NinchatTextViewModel(var label: String? = "", val isFormLikeQuestionnaire: Boolean = true) {
    fun parse(jsonObject: JSONObject?): NinchatTextViewModel {
        this.label = NinchatQuestionnaireItemGetter.getLabel(jsonObject)
        return this
    }
}