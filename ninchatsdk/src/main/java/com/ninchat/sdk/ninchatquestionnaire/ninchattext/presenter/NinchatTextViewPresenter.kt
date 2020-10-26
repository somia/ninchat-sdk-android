package com.ninchat.sdk.ninchatquestionnaire.ninchattext.presenter

import com.ninchat.sdk.ninchatquestionnaire.ninchattext.model.NinchatTextViewModel
import org.json.JSONObject


class NinchatTextViewPresenter(
        jsonObject: JSONObject?,
        isFormLikeQuestionnaire: Boolean = true,
        val iPresenter: INinchatTextViewPresenter,
) {
    private var ninchatTextViewModel = NinchatTextViewModel(
            isFormLikeQuestionnaire = isFormLikeQuestionnaire
    ).parse(jsonObject = jsonObject)

    fun updateModel(jsonObject: JSONObject?, isFormLikeQuestionnaire: Boolean) {
        ninchatTextViewModel = NinchatTextViewModel(isFormLikeQuestionnaire = isFormLikeQuestionnaire).parse(jsonObject)
    }

    fun renderCurrentView() {
        if (ninchatTextViewModel.isFormLikeQuestionnaire) {
            iPresenter.onUpdateFormView(label = ninchatTextViewModel.label)
        } else {
            iPresenter.onUpdateConversationView(label = ninchatTextViewModel.label)
        }
    }
}

interface INinchatTextViewPresenter {
    fun onUpdateFormView(label: String?)
    fun onUpdateConversationView(label: String?)
}