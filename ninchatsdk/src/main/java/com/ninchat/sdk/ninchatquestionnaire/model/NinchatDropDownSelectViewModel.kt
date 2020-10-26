package com.ninchat.sdk.ninchatquestionnaire.model

import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter
import org.json.JSONObject
import kotlin.math.max

data class NinchatDropDownSelectViewModel(
        var isFormLikeQuestionnaire: Boolean,
        var label: String? = "",
        var hasError: Boolean = false,
        var fireEvent: Boolean = false,
        var selectedIndex: Int = 0,
        var value: String? = "",
        var optionList: List<String> = listOf(),
) {

    fun parse(jsonObject: JSONObject?, isFormLikeQuestionnaire: Boolean = false): NinchatDropDownSelectViewModel {
        val optionList = arrayListOf("Select")
        NinchatQuestionnaireItemGetter.getOptions(jsonObject)?.let { options ->
            for (i in 0 until options.length()) {
                options.optJSONObject(i)?.let {
                    optionList.add(it.optString("label"))
                }
            }
        }
        this.optionList = optionList
        this.label = NinchatQuestionnaireItemGetter.getLabel(jsonObject)
        this.value = NinchatQuestionnaireItemGetter.getResultString(jsonObject)
        this.fireEvent = jsonObject?.optBoolean("fireEvent", false) ?: false
        this.selectedIndex =
                max(NinchatQuestionnaireItemGetter.getOptionIndex(jsonObject, this.value)+1, 0)
        this.translate()
        return this
    }

    private fun translate() {
        this.optionList = this.optionList.map {
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getTranslation(it) ?: it
        }
    }
}