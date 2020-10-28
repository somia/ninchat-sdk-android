package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.model

import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter
import org.json.JSONObject

data class NinchatRadioButtonModel(
        var label: String? = "",
        var hasError: Boolean = false,
        var isSelected: Boolean = false,
) {
    fun parse(jsonObject: JSONObject?) {
        label = NinchatQuestionnaireItemGetter.getLabel(jsonObject)
    }
}