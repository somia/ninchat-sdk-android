package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.model.NinchatRadioButtonListModel
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class NinchatRadioButtonListPresenter(
        var jsonObject: JSONObject? = null,
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
            viewCallback.onUpdateFormView(label = ninchatRadioButtonList.label ?: "", hasError = ninchatRadioButtonList.hasError)
        } else {
            viewCallback.onUpdateConversationView(label = ninchatRadioButtonList.label ?: "", hasError = ninchatRadioButtonList.hasError)
        }
    }

    fun handleOptionToggled(isSelected: Boolean, position: Int): Int {
        val previousPosition = ninchatRadioButtonList.position
        ninchatRadioButtonList.value = if (isSelected) ninchatRadioButtonList.getValue(position) else null
        ninchatRadioButtonList.position = if(isSelected) position else -1
        ninchatRadioButtonList.hasError = if(isSelected) false else ninchatRadioButtonList.hasError

        // update json model
        ninchatRadioButtonList.updateJson(jsonObject = jsonObject)
        if (isSelected) {
            mayBeFireEvent()
        }
        return previousPosition
    }

    fun isSelected(jsonObject: JSONObject?): Boolean {
        return jsonObject?.let {
            ninchatRadioButtonList.position != -1 && ninchatRadioButtonList.position == NinchatQuestionnaireItemGetter.getOptionPosition(it)
        } ?: false
    }

    private fun mayBeFireEvent() {
        if (ninchatRadioButtonList.fireEvent) {
            EventBus.getDefault().post(OnNextQuestionnaire(OnNextQuestionnaire.other));
        }
    }

    fun optionList() = ninchatRadioButtonList.optionList
    fun hasError(): Boolean = ninchatRadioButtonList.hasError
    internal fun getModel() = ninchatRadioButtonList
}

interface INinchatRadioButtonListPresenter {
    fun onUpdateFormView(label: String, hasError: Boolean)
    fun onUpdateConversationView(label: String, hasError: Boolean)
}