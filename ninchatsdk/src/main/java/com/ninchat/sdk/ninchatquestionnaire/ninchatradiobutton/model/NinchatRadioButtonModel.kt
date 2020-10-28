package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.model

import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter
import org.json.JSONObject

data class NinchatRadioButtonModel(
        var label: String? = "",
        var hasError: Boolean = false,
        var isSelected: Boolean = false,
) {
    fun parse(jsonObject: JSONObject?, hasError: Boolean = false, isSelected: Boolean = false) {
        this.label = NinchatQuestionnaireItemGetter.getLabel(jsonObject)
        this.hasError = hasError
        this.isSelected = isSelected
    }

    fun update(hasError: Boolean = false, isSelected: Boolean = false) {
        this.hasError = hasError
        this.isSelected = isSelected
    }
}