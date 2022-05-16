package com.ninchat.sdk.ninchatquestionnaire.ninchathyperlink.model

import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireConstants
import org.json.JSONObject

data class NinchatHyperlinkViewModel(
        var label: String = "",
        var hasLabel: Boolean = false,
        var enabled: Boolean,
        var hrefAttribute: String = "",
        var isSelected: Boolean = false,
        val position: Int,
) {
    fun parse(jsonObject: JSONObject?) {
        val currentLabel = jsonObject?.optString(NinchatQuestionnaireConstants.label) ?: ""
        this.hasLabel = currentLabel.isNotEmpty()
        this.hrefAttribute = jsonObject?.optString(NinchatQuestionnaireConstants.href) ?: ""
        this.label = if (this.hasLabel) currentLabel else this.hrefAttribute
        this.isSelected = jsonObject?.optBoolean("result") ?: false
        this.translate()
    }

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        this.enabled = enabled
        this.isSelected = jsonObject?.optBoolean("result") ?: false
    }

    private fun translate() {
        label = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getTranslation(label)
                ?: label
    }
}
