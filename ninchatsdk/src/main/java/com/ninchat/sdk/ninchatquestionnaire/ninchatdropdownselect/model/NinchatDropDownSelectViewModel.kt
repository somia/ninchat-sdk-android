package com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.model

import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemSetter
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

    fun parse(jsonObject: JSONObject?, isFormLikeQuestionnaire: Boolean = false) {
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
        this.hasError = NinchatQuestionnaireItemGetter.getError(jsonObject)
        this.fireEvent = jsonObject?.optBoolean("fireEvent", false) ?: false
        this.selectedIndex = max(optionList.indexOf(this.value), 0)
        this.translate()
    }

    fun update(jsonObject: JSONObject?) {
        this.value = NinchatQuestionnaireItemGetter.getResultString(jsonObject)
        this.hasError = NinchatQuestionnaireItemGetter.getError(jsonObject)
        this.selectedIndex = max(optionList.indexOf(this.value), 0)
        this.translate()
    }

    private fun translate() {
        this.optionList = this.optionList.map {
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getTranslation(it) ?: it
        }
    }

    @Deprecated("will be removed once converted to kotlin data model")
    fun updateJson(jsonObject: JSONObject?) {
        NinchatQuestionnaireItemSetter.setResult(jsonObject, this.value)
        NinchatQuestionnaireItemSetter.setError(jsonObject, this.hasError)
    }
}