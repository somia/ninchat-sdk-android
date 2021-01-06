package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.model

import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireConstants
import org.json.JSONObject

data class NinchatRadioButtonModel(
        var label: String? = "",
        var enabled: Boolean,
        var isSelected: Boolean = false,
) {
    fun parse(jsonObject: JSONObject?, hasError: Boolean = false, isSelected: Boolean = false) {
        this.label = jsonObject?.optString(NinchatQuestionnaireConstants.label)
        this.label = this.label?.let {
            if (jsonObject?.optBoolean("required", false) == true) "$it *" else it
        }
        this.isSelected = isSelected
    }

    fun update(isSelected: Boolean = false, enabled: Boolean) {
        this.isSelected = isSelected
        this.enabled = enabled
    }
}