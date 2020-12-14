package com.ninchat.sdk.ninchatquestionnaire.ninchattextviewholder.presenter

import com.ninchat.sdk.ninchatquestionnaire.ninchattextviewholder.model.NinchatTextViewModel
import org.json.JSONObject


class NinchatTextViewPresenter(
        jsonObject: JSONObject?,
        position: Int,
        isFormLikeQuestionnaire: Boolean = true,
        val iPresenter: INinchatTextViewPresenter,
) {
    private var model = NinchatTextViewModel(
            isFormLikeQuestionnaire = isFormLikeQuestionnaire, position = position).apply {
        parse(jsonObject = jsonObject)
    }

    fun renderCurrentView() {
        if (model.isFormLikeQuestionnaire) {
            iPresenter.onUpdateFormView(label = model.label)
        } else {
            iPresenter.onUpdateConversationView(label = model.label)
        }
    }
}

interface INinchatTextViewPresenter {
    fun onUpdateFormView(label: String?)
    fun onUpdateConversationView(label: String?)
}