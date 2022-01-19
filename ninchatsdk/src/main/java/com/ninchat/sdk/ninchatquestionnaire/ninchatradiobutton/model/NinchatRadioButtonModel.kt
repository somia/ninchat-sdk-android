package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.model

import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireConstants
import org.json.JSONObject

data class NinchatRadioButtonModel(
        var label: String? = "",
        var hasLabel: Boolean = false,
        var enabled: Boolean,
        var isSelected: Boolean = false,
        var hrefAttribute: String = "",
) {
    fun parse(jsonObject: JSONObject?, hasError: Boolean = false, isSelected: Boolean = false) {
        val currentLabel = jsonObject?.optString(NinchatQuestionnaireConstants.label) ?: ""
        this.hasLabel = currentLabel.isNotEmpty()
        this.hrefAttribute = jsonObject?.optString(NinchatQuestionnaireConstants.href) ?: ""
        this.label = if (this.hasLabel) currentLabel else this.hrefAttribute
        this.isSelected = isSelected
        this.translate()
    }

    fun update(isSelected: Boolean = false, enabled: Boolean) {
        this.isSelected = isSelected
        this.enabled = enabled
    }

    fun hasHrefAttribute(): Boolean {
        return this.hrefAttribute.isNotEmpty()
    }

    private fun translate() {
        label = label?.let {
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getTranslation(it)
                    ?: label
        } ?: label
    }
}