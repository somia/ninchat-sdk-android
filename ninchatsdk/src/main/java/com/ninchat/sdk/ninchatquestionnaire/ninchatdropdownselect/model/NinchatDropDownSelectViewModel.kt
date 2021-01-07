package com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.model

import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireConstants
import com.ninchat.sdk.ninchatquestionnaire.helper.fromJSONArray
import org.json.JSONObject
import kotlin.math.max

data class NinchatDropDownSelectViewModel(
        var isFormLikeQuestionnaire: Boolean,
        var label: String? = "",
        var hasError: Boolean = false,
        var fireEvent: Boolean = false,
        // which option is now selected
        var selectedIndex: Int = 0,
        var value: String? = "",
        var optionList: List<String> = listOf(),
        // position of the element in the answer list
        val position: Int,
        var enabled: Boolean
) {

    fun parse(jsonObject: JSONObject?, isFormLikeQuestionnaire: Boolean = false) {
        this.optionList = arrayListOf("Select").plus(
                fromJSONArray<JSONObject>(jsonObject?.optJSONArray("options"))
                        .map { (it as JSONObject).optString("label") })
        this.label = jsonObject?.optString(NinchatQuestionnaireConstants.label)
        this.label = this.label?.let {
            if (jsonObject?.optBoolean("required", false) == true) "$it *" else it
        }
        this.value = jsonObject?.optString(NinchatQuestionnaireConstants.result)
        this.hasError = jsonObject?.optBoolean(NinchatQuestionnaireConstants.hasError) ?: false
        this.fireEvent = jsonObject?.optBoolean(NinchatQuestionnaireConstants.fireEvent) ?: false
        this.selectedIndex = max(optionList.indexOf(this.value), 0)
        this.translate()
    }

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        this.enabled = enabled
        this.value = jsonObject?.optString("result")
        this.hasError = jsonObject?.optBoolean("hasError", false) ?: false
        this.selectedIndex = max(optionList.indexOf(this.value), 0)
        this.translate()
    }

    private fun translate() {
        this.optionList = this.optionList.map {
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getTranslation(it) ?: it
        }
    }
}