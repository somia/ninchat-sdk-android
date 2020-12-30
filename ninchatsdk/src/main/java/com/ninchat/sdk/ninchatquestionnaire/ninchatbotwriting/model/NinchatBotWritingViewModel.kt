package com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.model


import org.json.JSONObject

data class NinchatBotWritingViewModel(
        val position: Int,
        var enabled: Boolean,
        var label: String? = "",
        var imgUrl: String? = "",
) {

    fun parse(jsonObject: JSONObject?) {
        this.label = jsonObject?.optString("label")
        this.imgUrl = jsonObject?.optString("imgUrl")
    }

    fun update(enabled: Boolean) {
        this.enabled = enabled
    }
}