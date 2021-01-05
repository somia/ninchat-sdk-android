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

        if (model.isFormLikeQuestionnaire) {
            viewCallback.onUpdateFromView(
                    label = model.label ?: "",
                    enabled = model.enabled
            )
        } else {
            viewCallback.onUpdateConversationView(
                    label = model.label ?: "",
                    enabled = model.enabled
            )
        }
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
        val previousValue = model.value
        model.value = NinchatQuestionnaireNormalizer.sanitizeString(text)
        //if (previousValue != text)
        model.hasError = NinchatQuestionnaireJsonUtil.matchPattern(text, model.pattern) == false
        // check if there is any error
        viewCallback.onUpdateText(
                value = model.value ?: "",
                hasError = model.hasError
        )
        updateCallback.onUpdate(value = model.value, hasError = model.hasError, position = model.position)
    }

    override fun onFocusChange(hasFocus: Boolean) {
        model.hasFocus = hasFocus
        // error is a priority. If there is no error then may be change focus
        if (!model.hasError) {
            viewCallback.onUpdateFocus(hasFocus = hasFocus)
        }
    }
}

interface INinchatInputFieldViewPresenter {
    fun onUpdateFromView(label: String, enabled: Boolean)
    fun onUpdateConversationView(label: String, enabled: Boolean)
    fun onUpdateText(value: String, hasError: Boolean)
    fun onUpdateFocus(hasFocus: Boolean)
}


interface InputFieldUpdateListener {
    fun onUpdate(value: String?, hasError: Boolean, position: Int)
}