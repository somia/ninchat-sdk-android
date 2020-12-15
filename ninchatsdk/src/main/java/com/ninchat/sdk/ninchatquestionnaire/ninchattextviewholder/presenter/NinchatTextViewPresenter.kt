package com.ninchat.sdk.ninchatquestionnaire.ninchattextviewholder.presenter

import com.ninchat.sdk.ninchatquestionnaire.ninchattextviewholder.model.NinchatTextViewModel
import org.json.JSONObject


class NinchatTextViewPresenter(
        jsonObject: JSONObject?,
        position: Int,
        enabled: Boolean,
        isFormLikeQuestionnaire: Boolean = true,
        val iPresenter: INinchatTextViewPresenter,
) {
    private var model = NinchatTextViewModel(
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
            enabled = enabled,
            position = position).apply {
        parse(jsonObject = jsonObject)
    }

    fun renderCurrentView(enabled: Boolean) {
        model.enabled = enabled
        if (model.isFormLikeQuestionnaire) {
            iPresenter.onUpdateFormView(label = model.label, enabled = model.enabled)
        } else {
            iPresenter.onUpdateConversationView(label = model.label, enabled = model.enabled)
        }
    }
}

interface INinchatTextViewPresenter {
    fun onUpdateFormView(label: String?, enabled: Boolean)
    fun onUpdateConversationView(label: String?, enabled: Boolean)
}