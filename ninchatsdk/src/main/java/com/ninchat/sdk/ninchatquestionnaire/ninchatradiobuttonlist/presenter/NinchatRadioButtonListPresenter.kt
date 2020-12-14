package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.model.NinchatRadioButtonListModel
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class NinchatRadioButtonListPresenter(
        jsonObject: JSONObject? = null,
        isFormLikeQuestionnaire: Boolean,
        val viewCallback: INinchatRadioButtonListPresenter,
        val updateCallback: ButtonListUpdateListener,
        position: Int
) {
    private val model = NinchatRadioButtonListModel(
            isFormLikeQuestionnaire = isFormLikeQuestionnaire, position = position).apply {
        parse(jsonObject = jsonObject)
    }

    fun renderCurrentView(jsonObject: JSONObject? = null) {
        jsonObject?.let { model.update(jsonObject = jsonObject) }
        if (model.isFormLikeQuestionnaire) {
            viewCallback.onUpdateFormView(label = model.label ?: "", hasError = model.hasError)
        } else {
            viewCallback.onUpdateConversationView(label = model.label ?: "", hasError = model.hasError)
        }
    }

    fun handleOptionToggled(isSelected: Boolean, listPosition: Int): Int {
        val previousPosition = model.listPosition
        model.value = if (isSelected) model.getValue(listPosition) else null
        model.listPosition = if(isSelected) listPosition else -1
        model.hasError = if(isSelected) false else model.hasError

        updateCallback.onUpdate(value = model.value,
                sublistPosition = model.listPosition,
                hasError = model.hasError,
                position = model.position)
        if (isSelected) {
            mayBeFireEvent()
        }
        return previousPosition
    }

    fun isSelected(jsonObject: JSONObject?): Boolean {
        return jsonObject?.let {
            model.listPosition != -1 && model.listPosition == NinchatQuestionnaireItemGetter.getOptionPosition(it)
        } ?: false
    }

    private fun mayBeFireEvent() {
        if (model.fireEvent) {
            EventBus.getDefault().post(OnNextQuestionnaire(OnNextQuestionnaire.other));
        }
    }

    fun optionList() = model.optionList
    fun hasError(): Boolean = model.hasError
    internal fun getModel() = model
}

interface INinchatRadioButtonListPresenter {
    fun onUpdateFormView(label: String, hasError: Boolean)
    fun onUpdateConversationView(label: String, hasError: Boolean)
}

interface ButtonListUpdateListener{
    fun onUpdate(value: String?, sublistPosition: Int, hasError: Boolean, position: Int)
}