package com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.presenter

import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireNormalizer
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.model.NinchatInputFieldViewModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.view.INinchatInputFieldViewHolder
import org.json.JSONObject

class NinchatInputFieldViewPresenter(
    jsonObject: JSONObject?,
    isMultiline: Boolean,
    isFormLikeQuestionnaire: Boolean = true,
    val viewCallback: INinchatInputFieldViewPresenter,
    val updateCallback: InputFieldUpdateListener,
    position: Int,
    enabled: Boolean,
) : INinchatInputFieldViewHolder {
    private var model = NinchatInputFieldViewModel(
        isMultiline = isMultiline,
        isFormLikeQuestionnaire = isFormLikeQuestionnaire,
        position = position,
        enabled = enabled
    ).apply {
        parse(jsonObject = jsonObject)
    }


    fun renderCurrentView(jsonObject: JSONObject?, enabled: Boolean) {
        jsonObject?.let { model.update(jsonObject = jsonObject, enabled = enabled) }

        viewCallback.renderCommonView(
            label = model.label ?: "",
            enabled = model.enabled,
            isFormLike = model.isFormLikeQuestionnaire,
            isMultiline = model.isMultiline,
        )
        // update text change
        viewCallback.onUpdateText(
            value = model.value ?: "",
            hasError = model.hasError
        )
        // update focus
        if (!model.hasError) {
            viewCallback.onUpdateFocus(hasFocus = model.hasFocus)
        }
    }

    fun updateCurrentView(jsonObject: JSONObject?, enabled: Boolean) {
        jsonObject?.let { model.update(jsonObject = jsonObject, enabled = enabled) }

        viewCallback.updateCommonView(
            label = model.label ?: "",
            enabled = model.enabled,
            isFormLike = model.isFormLikeQuestionnaire,
            isMultiline = model.isMultiline,
        )
        // update text change
        viewCallback.onUpdateText(
            value = model.value ?: "",
            hasError = model.hasError
        )
        // update focus
        if (!model.hasError) {
            viewCallback.onUpdateFocus(hasFocus = model.hasFocus)
        }
    }

    fun getInputType(): Int = model.inputType
    fun isMultiline(): Boolean = model.isMultiline
    fun getInputValue(): String? = model.value

    override fun onTextChange(text: String?) {
        if (!model.hasFocus) return
        model.value = NinchatQuestionnaireNormalizer.sanitizeString(text)
        //if (previousValue != text)
        model.hasError = NinchatQuestionnaireJsonUtil.matchPattern(text, model.pattern) == false
        // check if there is any error
        viewCallback.onUpdateText(
            value = model.value ?: "",
            hasError = model.hasError
        )
        updateCallback.onUpdate(
            value = model.value,
            hasError = model.hasError,
            position = model.position
        )
    }

    override fun onFocusChange(hasFocus: Boolean) {
        model.hasFocus = hasFocus
        // error is a priority. If there is no error then may be change focus
        if (!model.hasError) {
            viewCallback.onUpdateFocus(hasFocus = hasFocus)
        }
    }

    fun position() = model.position
}

interface INinchatInputFieldViewPresenter {
    fun renderCommonView(isMultiline: Boolean, label: String, enabled: Boolean, isFormLike: Boolean)
    fun updateCommonView(isMultiline: Boolean, label: String, enabled: Boolean, isFormLike: Boolean)
    fun onUpdateText(value: String, hasError: Boolean)
    fun onUpdateFocus(hasFocus: Boolean)
}


interface InputFieldUpdateListener {
    fun onUpdate(value: String?, hasError: Boolean, position: Int)
}