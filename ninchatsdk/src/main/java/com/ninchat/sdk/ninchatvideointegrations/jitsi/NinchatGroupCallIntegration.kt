package com.ninchat.sdk.ninchatvideointegrations.jitsi

import android.view.View
import com.ninchat.sdk.ninchatvideointegrations.jitsi.model.NinchatGroupCallModel
import com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter.NinchatGroupCallPresenter
import com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter.OnClickListener
import kotlinx.android.synthetic.main.ninchat_join_end_conference.view.*

class NinchatGroupCallIntegration(
    private val joinConferenceView: View,
    private val jitsiVideoView: View,
    chatClosed: Boolean = false,
) {
    private val model = NinchatGroupCallModel(endConference = chatClosed).apply {
        parse()
    }
    private val presenter = NinchatGroupCallPresenter(model = model)
    private val onClickListener = OnClickListener(intervalInMs = 2000)

    init {
        presenter.renderView(joinConferenceView = joinConferenceView)
        attachHandler()
    }

    private fun attachHandler() {
        joinConferenceView.conference_join_button.setOnClickListener {
            onClickListener.onClickListener {
                presenter.onClickHandler()
            }
        }
    }

    fun updateView(chatClosed: Boolean) {
        model.update(endConference = chatClosed)
        presenter.renderView(joinConferenceView = joinConferenceView)
    }

    fun hideJoinMxzxz() {
        jitsiVideoView.visibility = View.VISIBLE
    }

    fun startJitsi(
        jitsiRoom: String,
        jitsiToken: String,
        jitsiServerPrefix: String,
    ) {

    }
}