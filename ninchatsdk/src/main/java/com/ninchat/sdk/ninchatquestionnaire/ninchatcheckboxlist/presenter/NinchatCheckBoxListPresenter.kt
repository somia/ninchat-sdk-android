package com.ninchat.sdk.ninchatquestionnaire.ninchatcheckboxlist.presenter

import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckboxlist.model.NinchatCheckBoxListModel
import org.json.JSONObject

class NinchatCheckBoxListPresenter(
        jsonObject: JSONObject? = null,
        isFormLikeQuestionnaire: Boolean,
        val viewCallback: INinchatCheckboxListPresenter,
        position: Int,
        enabled: Boolean,
) {
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
}

interface INinchatCheckboxListPresenter {
    fun onRenderView(optionsList: List<JSONObject>, enabled: Boolean, isFormView: Boolean)
    fun onUpdateView(optionsList: List<JSONObject>, enabled: Boolean, isFormView: Boolean)
}

interface CheckboxListUpdateListener {
    fun onUpdate(value: String?, sublistPosition: Int, hasError: Boolean, position: Int)
}