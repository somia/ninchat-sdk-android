package com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.model


import org.json.JSONObject

data class NinchatBotWritingViewModel(
        val position: Int,
        var enabled: Boolean,
        var loaded: Boolean? = false,
        var target: String? = "",
        var thankYouText: String? = "",
        var label: String? = "",
        var imgUrl: String? = "",
) {

    fun parse(jsonObject: JSONObject?) {
        this.label = jsonObject?.optString("label")?.let {
            if (it in listOf("null", "false", "")) null
            else it
        }
        this.imgUrl = jsonObject?.optString("imgUrl")?.let {
            if (it in listOf("null", "false", "")) null
            else it
        }
        this.thankYouText = jsonObject?.optString("thankYouText")?.let {
            if (it in listOf("null", "false", "")) null
            else it
        }
        this.target = jsonObject?.optString("target")?.let {
            if (it in listOf("null", "false", "")) null
            else it
        }
        this.loaded = jsonObject?.optBoolean("loaded", false) ?: false
    }

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        this.target = jsonObject?.optString("target")?.let {
            if (it in listOf("null", "false", "")) null
            else it
        }
        this.thankYouText = jsonObject?.optString("thankYouText")?.let {
            if (it in listOf("null", "false", "")) null
            else it
        }
        this.loaded = jsonObject?.optBoolean("loaded", false) ?: false
        this.enabled = enabled
    }
}