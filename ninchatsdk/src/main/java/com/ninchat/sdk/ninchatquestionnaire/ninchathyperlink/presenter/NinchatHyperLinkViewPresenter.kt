package com.ninchat.sdk.ninchatquestionnaire.ninchathyperlink.presenter

import com.ninchat.sdk.ninchatquestionnaire.ninchathyperlink.model.NinchatHyperlinkViewModel
import org.json.JSONObject

class NinchatHyperLinkViewPresenter(
        jsonObject: JSONObject?,
        position: Int,
        enabled: Boolean,
        val viewCallback: INinchatHyperLinkPresenter,
        val updateCallback: HyperLinkClickListener,
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
        model.update(jsonObject = jsonObject, enabled = enabled)
        viewCallback.onUpdateView(
                isSelected = model.isSelected,
                enabled = model.enabled,
        )
    }

    fun onLinkClicked() {
        model.isSelected = true
        viewCallback.onClickedView()
        updateCallback.onUpdate(value = true, position = model.position)
    }
}

interface INinchatHyperLinkPresenter {
    fun onRenderView(label: String, isSelected: Boolean, enabled: Boolean)
    fun onUpdateView(isSelected: Boolean, enabled: Boolean)
    fun onClickedView()
}

interface HyperLinkClickListener {
    fun onUpdate(value: Boolean, position: Int)
}