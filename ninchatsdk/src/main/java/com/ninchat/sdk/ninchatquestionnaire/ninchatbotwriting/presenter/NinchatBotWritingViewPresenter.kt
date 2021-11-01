package com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.presenter

import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.model.NinchatBotWritingViewModel
import org.json.JSONObject

class NinchatBotWritingViewPresenter(
    jsonObject: JSONObject?,
    position: Int,
    enabled: Boolean,
    val updateCallback: BotWritingCompleteListener,
    val presenter: INinchatBotWritingViewPresenter,
) {
    private var model = NinchatBotWritingViewModel(position = position, enabled = enabled).apply {
        parse(jsonObject = jsonObject)
    }

    fun updateModel(jsonObject: JSONObject?, enabled: Boolean) {
        model.update(jsonObject = jsonObject, enabled = enabled)
    }

    fun renderCurrentView() {
        presenter.onRenderView(label = model.label, imgUrl = model.imgUrl, enabled = model.enabled)
    }

    fun updateCurrentView() {
        presenter.onUpdateView(label = model.label, imgUrl = model.imgUrl, enabled = model.enabled)
    }

    fun onAnimationComplete() {
        updateCallback.onCompleteLoading(
            target = model.target,
            thankYouText = model.thankYouText,
            loaded = model.loaded ?: false,
            position = model.position,
        )
    }

    fun isLoaded(): Boolean = model.loaded == true

    fun setLoaded() {
        model.loaded = true
    }
}

interface INinchatBotWritingViewPresenter {
    fun onRenderView(label: String?, imgUrl: String?, enabled: Boolean)
    fun onUpdateView(label: String?, imgUrl: String?, enabled: Boolean)
}

interface BotWritingCompleteListener {
    fun onCompleteLoading(target: String?, thankYouText: String?, loaded: Boolean, position: Int)
}