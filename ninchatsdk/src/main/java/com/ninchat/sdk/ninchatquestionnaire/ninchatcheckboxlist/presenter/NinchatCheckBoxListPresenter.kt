package com.ninchat.sdk.ninchatquestionnaire.ninchatcheckboxlist.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.presenter.CheckboxUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckboxlist.model.NinchatCheckBoxListModel
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class NinchatCheckBoxListPresenter(
        jsonObject: JSONObject? = null,
        isFormLikeQuestionnaire: Boolean,
        val viewCallback: INinchatCheckboxListPresenter,
        val updateCallback: CheckboxListUpdateListener,
        position: Int,
        enabled: Boolean,
) : CheckboxUpdateListener {
    private val model = NinchatCheckBoxListModel(
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
            position = position,
            enabled = enabled).apply {
        parse(jsonObject = jsonObject)
    }

    fun renderCurrentView(jsonObject: JSONObject? = null, enabled: Boolean) {
        viewCallback.onRenderView(
                optionsList = model.optionList,
                enabled = enabled,
                isFormView = model.isFormLikeQuestionnaire
        )
    }

    fun updateCurrentView(jsonObject: JSONObject? = null, enabled: Boolean) {
        jsonObject?.let {
            model.update(jsonObject = jsonObject, enabled = enabled)
        }
        viewCallback.onUpdateView(
                optionsList = model.optionList,
                enabled = enabled,
                isFormView = model.isFormLikeQuestionnaire
        )
    }

    fun get(position: Int): JSONObject = model.optionList.getOrNull(position) ?: JSONObject()
    fun isEnabled(): Boolean = model.enabled
    fun size(): Int = model.optionList.size
    override fun onUpdate(value: Boolean, hasError: Boolean, fireEvent: Boolean, position: Int) {
        updateCallback.onUpdate(value = value, sublistPosition = position, hasError = hasError, position = model.position)
        // if can fire a event and value is selected
        if (fireEvent && value) {
            EventBus.getDefault().post(OnNextQuestionnaire(OnNextQuestionnaire.other));
        }
    }
}

interface INinchatCheckboxListPresenter {
    fun onRenderView(optionsList: List<JSONObject>, enabled: Boolean, isFormView: Boolean)
    fun onUpdateView(optionsList: List<JSONObject>, enabled: Boolean, isFormView: Boolean)
}

interface CheckboxListUpdateListener {
    fun onUpdate(value: Boolean, sublistPosition: Int, hasError: Boolean, position: Int)
}