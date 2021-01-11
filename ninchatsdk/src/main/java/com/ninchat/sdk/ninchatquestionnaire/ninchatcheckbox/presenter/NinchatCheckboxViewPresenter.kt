package com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.presenter

import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.model.NinchatCheckboxModel
import org.json.JSONObject

class NinchatCheckboxViewPresenter(
        jsonObject: JSONObject?,
        position: Int,
        val presenter: INinchatCheckboxViewPresenter,
        val checkboxToggleListener: CheckboxUpdateListener,
        enabled: Boolean,
) {
    private var model = NinchatCheckboxModel(enabled = enabled, position = position).apply {
        parse(jsonObject = jsonObject)
    }

    fun renderView() {
        presenter.onRenderView(
                label = model.label,
                isChecked = model.result,
                hasError = model.hasError,
                enabled = model.enabled,
        )
    }

    fun updateView(jsonObject: JSONObject?) {
        model.parse(jsonObject = jsonObject)
        presenter.onUpdateView(
                isChecked = model.result,
                hasError = model.hasError,
                enabled = model.enabled,
        )
    }

    fun onCheckBoxToggled() {
        model.result = !model.result
        presenter.onToggleView(
                isChecked = model.result,
                hasError = model.hasError,
                enabled = model.enabled,
        )
        checkboxToggleListener.onUpdate(
                value = model.result,
                hasError = model.hasError,
                fireEvent = model.fireEvent,
                position = model.position)
    }

}

interface INinchatCheckboxViewPresenter {
    fun onRenderView(label: String?, isChecked: Boolean, hasError: Boolean, enabled: Boolean)
    fun onUpdateView(isChecked: Boolean, hasError: Boolean, enabled: Boolean)
    fun onToggleView(isChecked: Boolean, hasError: Boolean, enabled: Boolean)
}

interface CheckboxUpdateListener {
    fun onUpdate(value: Boolean, hasError: Boolean, fireEvent: Boolean, position: Int)
}