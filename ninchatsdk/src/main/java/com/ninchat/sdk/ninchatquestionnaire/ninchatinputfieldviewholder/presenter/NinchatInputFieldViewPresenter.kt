package com.ninchat.sdk.ninchatquestionnaire.ninchatinputfield.presenter

import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfield.model.NinchatInputFieldViewModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfield.view.INinchatInputFieldViewHolder
import org.json.JSONObject

class NinchatInputFieldViewPresenter(
        val jsonObject: JSONObject?,
        isMultiline: Boolean,
        isFormLikeQuestionnaire: Boolean = true,
        val viewCallback: INinchatInputFieldViewPresenter,
) : INinchatInputFieldViewHolder {
    private var model = NinchatInputFieldViewModel(
            isMultiline = isMultiline,
            isFormLikeQuestionnaire = isFormLikeQuestionnaire
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
        model.value = NinchatQuestionnaireMiscUtil.sanitizeString(text)
        model.hasError = NinchatQuestionnaireMiscUtil.matchPattern(text, model.pattern) == false
        // check if there is any error
        viewCallback.onUpdateText(
                value = model.value ?: "",
                hasError = model.hasError
        )
        // update json model
        model.updateJson(jsonObject = jsonObject)
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