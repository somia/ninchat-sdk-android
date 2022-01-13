package com.ninchat.sdk.ninchatquestionnaire.ninchathyperlink.presenter

import com.ninchat.sdk.ninchatquestionnaire.ninchathyperlink.model.NinchatHyperlinkViewModel
import org.json.JSONObject

class NinchatHyperLinkViewPresenter(
        jsonObject: JSONObject?,
        position: Int,
        enabled: Boolean,
        val viewCallback: INinchatHyperLinkPresenter,
) {
    private val model = NinchatHyperlinkViewModel(
            enabled = enabled,
            position = position,
    ).apply {
        parse(jsonObject = jsonObject)
    }

    fun renderCurrentView() {
        viewCallback.onRenderView(
                label = model.label,
                isSelected = model.isSelected,
                enabled = model.enabled,
        )
    }

    fun updateCurrentView(jsonObject: JSONObject?, enabled: Boolean) {
        model.update(enabled = enabled)
        viewCallback.onUpdateView(
                isSelected = model.isSelected,
                enabled = model.enabled,
        )
    }
}

interface INinchatHyperLinkPresenter {
    fun onRenderView(label: String, isSelected: Boolean, enabled: Boolean)
    fun onUpdateView(isSelected: Boolean, enabled: Boolean)
}