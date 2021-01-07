package com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.model.NinchatCheckboxViewModel
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class NinchatCheckboxViewPresenter(
        jsonObject: JSONObject?,
        isFormLikeQuestionnaire: Boolean = true,
        val iPresent: INinchatCheckboxViewPresenter,
        val updateCallback: CheckboxUpdateListener,
        position: Int,
        enabled: Boolean,
) {
    private var model = NinchatCheckboxViewModel(
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
            position = position,
            enabled = enabled,
    ).apply {
        parse(jsonObject = jsonObject)
    }


    fun renderCurrentView(jsonObject: JSONObject? = null, enabled: Boolean) {
        jsonObject?.let {
            model.update(jsonObject = jsonObject, enabled = enabled)
        }
        if (model.isFormLikeQuestionnaire) {
            // render form like
            iPresent.onRenderFromView(
                    label = model.label,
                    isChecked = model.isChecked,
                    hasError = model.hasError,
                    enabled = enabled
            )
            return
        }
        // render conversation like
        iPresent.onRenderConversationView(
                label = model.label,
                isChecked = model.isChecked,
                hasError = model.hasError,
                enabled = enabled
        )
    }

    fun updateCurrentView(jsonObject: JSONObject? = null, enabled: Boolean) {
        jsonObject?.let {
            model.update(jsonObject = jsonObject, enabled = enabled)
        }
        if (model.isFormLikeQuestionnaire) {
            // render form like
            iPresent.onUpdateFromView(
                    label = model.label,
                    isChecked = model.isChecked,
                    hasError = model.hasError,
                    enabled = enabled
            )
            return
        }
        // render conversation like
        iPresent.onUpdateConversationView(
                label = model.label,
                isChecked = model.isChecked,
                hasError = model.hasError,
                enabled = enabled
        )
    }

    fun handleCheckBoxToggled() {
        model.isChecked = !model.isChecked
        model.hasError = false
        iPresent.onCheckBoxToggled(
                isChecked = model.isChecked,
                hasError = model.hasError)
        updateCallback.onUpdate(value = model.isChecked, hasError = model.hasError, position = model.position)
        if (!model.isChecked) return
        mayBeFireEvent()
    }

    private fun mayBeFireEvent() {
        if (!model.fireEvent) return
        EventBus.getDefault().post(OnNextQuestionnaire(OnNextQuestionnaire.other))
    }
}

interface INinchatCheckboxViewPresenter {
    fun onRenderFromView(label: String?, isChecked: Boolean, hasError: Boolean, enabled: Boolean)
    fun onRenderConversationView(label: String?, isChecked: Boolean, hasError: Boolean, enabled: Boolean)
    fun onUpdateFromView(label: String?, isChecked: Boolean, hasError: Boolean, enabled: Boolean)
    fun onUpdateConversationView(label: String?, isChecked: Boolean, hasError: Boolean, enabled: Boolean)
    fun onCheckBoxToggled(isChecked: Boolean, hasError: Boolean)
}

interface CheckboxUpdateListener {
    fun onUpdate(value: Boolean, hasError: Boolean, position: Int)
}