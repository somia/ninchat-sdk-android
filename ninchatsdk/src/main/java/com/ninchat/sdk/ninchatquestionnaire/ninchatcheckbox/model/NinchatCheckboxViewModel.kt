package com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.model

import com.ninchat.sdk.NinchatSessionManager
import org.json.JSONObject


data class NinchatCheckboxModel(
        var enabled: Boolean = false,
        var label: String? = "",
        var result: Boolean = false,
        var name: String? = "",
        var hasError: Boolean = false,
        var fireEvent: Boolean? = false,
) {
    fun parse(jsonObject: JSONObject?) {
        label = jsonObject?.optString("label")?.let {
            if (jsonObject.optBoolean("required", false)) "$it *" else it
        }
        result = jsonObject?.optBoolean("result") ?: false
        name = jsonObject?.optString("name")
        hasError = jsonObject?.optBoolean("hasError") ?: false
        fireEvent = jsonObject?.optBoolean("fireEvent") ?: false
        translate()
    }

    private fun translate() {
        // translate back and next button label
        label = label?.let {
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getTranslation(it)
                    ?: label
        } ?: label
    }

}