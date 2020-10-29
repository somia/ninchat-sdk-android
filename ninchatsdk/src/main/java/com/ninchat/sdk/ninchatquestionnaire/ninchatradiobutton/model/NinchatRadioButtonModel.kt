package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.model

import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter
import org.json.JSONObject

data class NinchatRadioButtonModel(
        var label: String? = "",
        var isSelected: Boolean = false,
) {
    fun parse(jsonObject: JSONObject?, hasError: Boolean = false, isSelected: Boolean = false) {
        this.label = NinchatQuestionnaireItemGetter.getLabel(jsonObject)
        this.isSelected = isSelected
    }

    fun update(isSelected: Boolean = false) {
        this.isSelected = isSelected
    }
}