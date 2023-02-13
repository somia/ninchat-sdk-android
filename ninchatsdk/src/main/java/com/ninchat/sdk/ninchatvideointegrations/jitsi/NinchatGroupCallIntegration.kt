package com.ninchat.sdk.ninchatvideointegrations.jitsi

import android.view.View
import com.ninchat.sdk.ninchatvideointegrations.jitsi.model.NinchatGroupCallModel
import com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter.NinchatGroupCallPresenter
import com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter.OnClickListener
import kotlinx.android.synthetic.main.ninchat_join_end_conference.view.*

class NinchatGroupCallIntegration(
    private val videoContainer: View,
    chatClosed: Boolean = false,
) {
    private val model = NinchatGroupCallModel(endConference = chatClosed).apply {
        parse()
    }
    private val presenter = NinchatGroupCallPresenter(model = model)
    private val onClickListener = OnClickListener(intervalInMs = 2000)

    init {
        presenter.renderView(videoContainer = videoContainer)
        attachHandler()
    }

    private fun attachHandler() {
        videoContainer.conference_join_button.setOnClickListener {
            onClickListener.onClickListener {
                presenter.onClickHandler()
            }
        }
    }

    fun updateView(chatClosed: Boolean) {
        model.update(endConference = chatClosed)
        presenter.renderView(videoContainer = videoContainer)
    }

    fun startJitsi(
        jitsiRoom: String,
        jitsiToken: String,
        jitsiServerPrefix: String,
    ) {

    }
}