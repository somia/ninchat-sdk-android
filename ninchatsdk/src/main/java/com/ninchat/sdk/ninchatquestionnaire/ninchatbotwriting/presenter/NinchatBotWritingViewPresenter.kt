package com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.presenter

import com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.model.NinchatBotWritingViewModel
import org.json.JSONObject

class NinchatBotWritingViewPresenter(
        jsonObject: JSONObject?,
        position: Int,
        enabled: Boolean,
        val updateCallback: ((Int) -> Unit)?,
        val presenter: INinchatBotWritingViewPresenter,
) {
    private var model = NinchatBotWritingViewModel(position = position, enabled = enabled).apply {
        parse(jsonObject = jsonObject)
    }

    fun updateModel(jsonObject: JSONObject?, enabled: Boolean) {
        model.update(jsonObject = jsonObject, enabled = enabled)
    }

    fun renderCurrentView() {
        presenter.onUpdateView(label = model.label, imgUrl = model.imgUrl, enabled = model.enabled)
    }

    fun onAnimationComplete() {
        if (model.loaded == true) return
        updateCallback?.let {
            it(model.position)
        }
    }

    fun isLoaded(): Boolean = model.loaded == true
}

interface INinchatBotWritingViewPresenter {
    fun onUpdateView(label: String?, imgUrl: String?, enabled: Boolean)
}