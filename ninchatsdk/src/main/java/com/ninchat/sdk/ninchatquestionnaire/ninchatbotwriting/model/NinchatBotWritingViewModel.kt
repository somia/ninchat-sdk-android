package com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.model


import org.json.JSONObject

data class NinchatBotWritingViewModel(
        val position: Int,
        var enabled: Boolean
) {

    fun parse(jsonObject: JSONObject?) {

    }

    fun update(enabled: Boolean) {
        this.enabled = enabled
    }
}