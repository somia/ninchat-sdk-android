package com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.presenter

import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.model.NinchatCheckboxModel
import org.json.JSONObject

class NinchatCheckboxViewPresenter(
        jsonObject: JSONObject?,
        val presenter: INinchatCheckboxViewPresenter,
        enabled: Boolean,
) {
    private var model = NinchatCheckboxModel(enabled = enabled).apply {
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

}

interface INinchatCheckboxViewPresenter {
    fun onRenderView(label: String?, isChecked: Boolean, hasError: Boolean, enabled: Boolean)
    fun onUpdateView(isChecked: Boolean, hasError: Boolean, enabled: Boolean)
}

interface CheckboxUpdateListener {
    fun onUpdate(value: Boolean, hasError: Boolean, position: Int)
}