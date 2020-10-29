package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.model.NinchatRadioButtonListModel
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class NinchatRadioButtonListPresenter(
        jsonObject: JSONObject?,
        isFormLikeQuestionnaire: Boolean,
        val viewCallback: INinchatRadioButtonListPresenter,
) {
    private val ninchatRadioButtonList = NinchatRadioButtonListModel(
            isFormLikeQuestionnaire = isFormLikeQuestionnaire).apply {
        parse(jsonObject = jsonObject)
    }

    fun renderCurrentView(jsonObject: JSONObject? = null) {
        jsonObject?.let { ninchatRadioButtonList.update(jsonObject = jsonObject) }
        if (ninchatRadioButtonList.isFormLikeQuestionnaire) {
            viewCallback.onUpdateFormView(label = ninchatRadioButtonList.label ?: "")
        } else {
            viewCallback.onUpdateConversationView(label = ninchatRadioButtonList.label ?: "")
        }
    }

    fun handleOptionToggled(isSelected: Boolean, label: String?): Int {
        val previouslySelected = ninchatRadioButtonList.value
        ninchatRadioButtonList.value = if (isSelected) label else ""
        return ninchatRadioButtonList.getIndex(previouslySelected)
    }

    fun isSelected(jsonObject: JSONObject?): Boolean {
        return jsonObject?.let {
            ninchatRadioButtonList.value == NinchatQuestionnaireItemGetter.getResultString(it) ?: "~"
        } ?: false
    }

    fun mayBeFireEvent() {
        if (ninchatRadioButtonList.fireEvent) {
            EventBus.getDefault().post(OnNextQuestionnaire(OnNextQuestionnaire.other));
        }
    }

    fun optionList() = ninchatRadioButtonList.optionList
    fun hasError(): Boolean = ninchatRadioButtonList.hasError ?: false
}

interface INinchatRadioButtonListPresenter {
    fun onUpdateFormView(label: String)
    fun onUpdateConversationView(label: String)
}