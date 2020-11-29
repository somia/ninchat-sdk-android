package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.model

import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireConstants
import org.json.JSONObject

data class NinchatRadioButtonModel(
        var label: String? = "",
        var isSelected: Boolean = false,
) {
    fun parse(jsonObject: JSONObject?, hasError: Boolean = false, isSelected: Boolean = false) {
        this.label = jsonObject?.optString(NinchatQuestionnaireConstants.label)
        this.isSelected = isSelected
    }

    fun update(isSelected: Boolean = false) {
        this.isSelected = isSelected
    }
}