package com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.model.NinchatCheckboxViewModel
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class NinchatCheckboxViewPresenter(
        val jsonObject: JSONObject?,
        isFormLikeQuestionnaire: Boolean = true,
        private val iPresent: INinchatCheckboxViewPresenter,
) {
    private var ninchatCheckboxViewModel = NinchatCheckboxViewModel(
            isFormLikeQuestionnaire = isFormLikeQuestionnaire
    ).apply {
        parse(jsonObject = jsonObject)
    }


    fun renderCurrentView(jsonObject: JSONObject? = null) {
        jsonObject?.let {
            ninchatCheckboxViewModel.update(jsonObject = jsonObject)
        }
        if (ninchatCheckboxViewModel.isFormLikeQuestionnaire) {
            // render form like
            iPresent.onUpdateFromView(
                    label = ninchatCheckboxViewModel.label,
                    isChecked = ninchatCheckboxViewModel.isChecked,
                    hasError = ninchatCheckboxViewModel.hasError
            )
            return
        }
        // render conversation like
        iPresent.onUpdateConversationView(
                label = ninchatCheckboxViewModel.label,
                isChecked = ninchatCheckboxViewModel.isChecked,
                hasError = ninchatCheckboxViewModel.hasError
        )
    }

    fun handleCheckBoxToggled(isChecked: Boolean) {
        ninchatCheckboxViewModel.isChecked = isChecked
        ninchatCheckboxViewModel.hasError = false
        iPresent.onCheckBoxToggled(
                isChecked = ninchatCheckboxViewModel.isChecked,
                hasError = ninchatCheckboxViewModel.hasError)

        // update json model
        ninchatCheckboxViewModel.updateJson(jsonObject = jsonObject)
        if (!isChecked) return
        mayBeFireEvent()
    }

    private fun mayBeFireEvent() {
        if (!ninchatCheckboxViewModel.fireEvent) return
        EventBus.getDefault().post(OnNextQuestionnaire(OnNextQuestionnaire.other))
    }
}

interface INinchatCheckboxViewPresenter {
    fun onUpdateFromView(label: String?, isChecked: Boolean, hasError: Boolean)
    fun onUpdateConversationView(label: String?, isChecked: Boolean, hasError: Boolean)
    fun onCheckBoxToggled(isChecked: Boolean, hasError: Boolean)
}