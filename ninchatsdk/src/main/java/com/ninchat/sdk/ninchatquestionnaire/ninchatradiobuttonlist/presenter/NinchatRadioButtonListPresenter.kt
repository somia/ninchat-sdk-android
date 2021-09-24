package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.model.NinchatRadioButtonListModel
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class NinchatRadioButtonListPresenter(
    jsonObject: JSONObject? = null,
    isFormLikeQuestionnaire: Boolean,
    val viewCallback: INinchatRadioButtonListPresenter,
    val updateCallback: ButtonListUpdateListener,
    position: Int,
    enabled: Boolean,
) {
    private val model = NinchatRadioButtonListModel(
        isFormLikeQuestionnaire = isFormLikeQuestionnaire,
        position = position,
        enabled = enabled
    ).apply {
        parse(jsonObject = jsonObject)
    }

    fun renderCurrentView(jsonObject: JSONObject? = null, enabled: Boolean) {
        jsonObject?.let {
            model.update(jsonObject = it, enabled = enabled)
        }
        viewCallback.renderCommon(
            label = model.label
                ?: "", hasError = model.hasError, enabled = model.enabled,
            isFormLike = model.isFormLikeQuestionnaire
        )
    }

    fun updateCurrentView(jsonObject: JSONObject? = null, enabled: Boolean) {
        jsonObject?.let {
            model.update(jsonObject = jsonObject, enabled = enabled)
        }
        viewCallback.updateCommon(
            label = model.label
                ?: "", hasError = model.hasError, enabled = model.enabled,
            isFormLike = model.isFormLikeQuestionnaire
        )
    }

    fun handleOptionToggled(isSelected: Boolean, listPosition: Int): Int {
        val previousPosition = model.listPosition
        model.value = if (isSelected) model.getValue(listPosition) else null
        model.listPosition = if (isSelected) listPosition else -1
        model.hasError = if (isSelected) false else model.hasError

        updateCallback.onUpdate(
            value = model.value,
            sublistPosition = model.listPosition,
            hasError = model.hasError,
            position = model.position
        )
        if (isSelected) {
            mayBeFireEvent()
        }
        return previousPosition
    }

    fun isSelected(currentPosition: Int): Boolean {
        return model.listPosition != -1 && model.listPosition == currentPosition
    }

    private fun mayBeFireEvent() {
        if (model.fireEvent) {
            EventBus.getDefault().post(OnNextQuestionnaire(OnNextQuestionnaire.other));
        }
    }

    fun optionList() = model.optionList
    fun hasError(): Boolean = model.hasError
    fun isEnabled(): Boolean = model.enabled
    internal fun getModel() = model
}

interface INinchatRadioButtonListPresenter {
    fun renderCommon(label: String, hasError: Boolean, enabled: Boolean, isFormLike: Boolean)
    fun updateCommon(label: String, hasError: Boolean, enabled: Boolean, isFormLike: Boolean)
}

interface ButtonListUpdateListener {
    fun onUpdate(value: String?, sublistPosition: Int, hasError: Boolean, position: Int)
}