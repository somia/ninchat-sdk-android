package com.ninchat.sdk.ninchatquestionnaire.ninchatinputfield.presenter

import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireNormalizer
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfield.model.NinchatInputFieldViewModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfield.view.INinchatInputFieldViewHolder
import org.json.JSONObject

class NinchatInputFieldViewPresenter(
        jsonObject: JSONObject?,
        isMultiline: Boolean,
        isFormLikeQuestionnaire: Boolean = true,
        val viewCallback: INinchatInputFieldViewPresenter,
        val updateCallback: InputFieldUpdateListener,
        position: Int
) : INinchatInputFieldViewHolder {
    private var model = NinchatInputFieldViewModel(
            isMultiline = isMultiline,
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
            position = position
    ).apply {
        parse(jsonObject = jsonObject)
    }


    fun renderCurrentView(jsonObject: JSONObject?) {
        jsonObject?.let { model.update(jsonObject = jsonObject) }
        
        if (model.isFormLikeQuestionnaire) {
            viewCallback.onUpdateFromView(
                    label = model.label ?: "",
            )
        } else {
            viewCallback.onUpdateConversationView(
                    label = model.label ?: "",
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

    override fun onTextChange(text: String?) {
        model.value = NinchatQuestionnaireNormalizer.sanitizeString(text)
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
    fun onUpdateFromView(label: String)
    fun onUpdateConversationView(label: String)
    fun onUpdateText(value: String, hasError: Boolean)
    fun onUpdateFocus(hasFocus: Boolean)
}


interface InputFieldUpdateListener{
    fun onUpdate(value: String?, hasError: Boolean, position: Int)
}