package com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.model


import org.json.JSONObject

data class NinchatBotWritingViewModel(
        val position: Int,
        var enabled: Boolean,
        var loaded: Boolean? = false,
        var label: String? = "",
        var imgUrl: String? = "",
) {

    fun parse(jsonObject: JSONObject?) {
        this.label = jsonObject?.optString("label")
        this.imgUrl = jsonObject?.optString("imgUrl")
        this.loaded = jsonObject?.optBoolean("loaded")
    }

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        this.loaded = jsonObject?.optBoolean("loaded")
        this.enabled = enabled
    }
}