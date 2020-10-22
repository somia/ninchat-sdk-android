package com.ninchat.sdk.ninchatquestionnaire.presenter

import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil
import com.ninchat.sdk.ninchatquestionnaire.model.NinchatInputFieldViewModel
import com.ninchat.sdk.ninchatquestionnaire.view.INinchatInputFieldViewHolder
import org.json.JSONObject

class NinchatInputFieldViewPresenter(
        jsonObject: JSONObject?,
        isMultiline: Boolean,
        isFormLikeQuestionnaire: Boolean = true,
        val viewCallback: INinchatInputFieldViewPresenter,
) : INinchatInputFieldViewHolder {
    private var ninchatInputFieldViewModel = NinchatInputFieldViewModel(
            isMultiline = isMultiline,
            isFormLikeQuestionnaire = isFormLikeQuestionnaire
    ).parse(jsonObject = jsonObject)


    fun renderCurrentView() {
        if (ninchatInputFieldViewModel.isFormLikeQuestionnaire) {
            viewCallback.onUpdateFromView(
                    label = ninchatInputFieldViewModel.label ?: "",
            )
            return
        }
        viewCallback.onUpdateConversationView(
                label = ninchatInputFieldViewModel.label ?: "",
        )
        // update text change
        viewCallback.onUpdateText(
                value = ninchatInputFieldViewModel.value ?: "",
                hasError = ninchatInputFieldViewModel.hasError
        )
        // update focus
        if (!ninchatInputFieldViewModel.hasError) {
            viewCallback.onUpdateFocus(hasFocus = ninchatInputFieldViewModel.hasFocus)
        }

    }

    fun getInputType(): Int = ninchatInputFieldViewModel.inputType
    fun isMultiline(): Boolean = ninchatInputFieldViewModel.isMultiline

    override fun onTextChange(text: String?) {
        ninchatInputFieldViewModel.value = NinchatQuestionnaireMiscUtil.sanitizeString(text)
        ninchatInputFieldViewModel.hasError = NinchatQuestionnaireMiscUtil.matchPattern(text, ninchatInputFieldViewModel.pattern)
        // check if there is any error
        viewCallback.onUpdateText(
                value = ninchatInputFieldViewModel.value ?: "",
                hasError = ninchatInputFieldViewModel.hasError
        )
    }

    override fun onFocusChange(hasFocus: Boolean) {
        ninchatInputFieldViewModel.hasFocus = hasFocus
        // error is a priority. If there is no error then may be change focus
        if (!ninchatInputFieldViewModel.hasError) {
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