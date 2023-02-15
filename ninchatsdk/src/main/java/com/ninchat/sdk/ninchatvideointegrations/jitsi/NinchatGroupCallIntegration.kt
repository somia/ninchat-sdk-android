package com.ninchat.sdk.ninchatvideointegrations.jitsi

import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.ninchat.sdk.ninchatvideointegrations.jitsi.model.NinchatGroupCallModel
import com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter.NinchatGroupCallPresenter
import com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter.OnClickListener
import kotlinx.android.synthetic.main.ninchat_join_end_conference.view.*
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetView
import java.net.URL

class NinchatGroupCallIntegration(
    private val joinConferenceView: View,
    private val jitsiFrameLayout: FrameLayout,
    private val jitsiMeetView: JitsiMeetView,
    chatClosed: Boolean = false,
) {
    private val model = NinchatGroupCallModel(endConference = chatClosed).apply {
        parse()
    }
    private val presenter = NinchatGroupCallPresenter(model = model)
    private val onClickListener = OnClickListener(intervalInMs = 2000)

    init {
        presenter.renderView(joinConferenceView = joinConferenceView, jitsiFrameLayout = jitsiFrameLayout)
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
        presenter.renderView(joinConferenceView = joinConferenceView, jitsiFrameLayout = jitsiFrameLayout)
    }

    fun startJitsi(
        jitsiRoom: String,
        jitsiToken: String,
        jitsiServerAddress: String,
    ) {
        Log.d("JitsiMeet", "jitsiMeetServerAddress: $jitsiServerAddress $jitsiRoom $jitsiToken ")
        val options: JitsiMeetConferenceOptions = JitsiMeetConferenceOptions.Builder()
            .setRoom(jitsiRoom)
            .setToken(jitsiToken)
            .setServerURL(URL(jitsiServerAddress))
            .setFeatureFlag("pip.enabled", false)
            .setFeatureFlag("add-people.enabled", false)
            .setFeatureFlag("calendar.enabled", false)
            .setFeatureFlag("chat.enabled", false)
            .setFeatureFlag("filmstrip.enabled", false)
            .setFeatureFlag("invite.enabled", false)
            .setFeatureFlag("kick-out.enabled", false)
            .setFeatureFlag("live-streaming.enabled", false)
            .setFeatureFlag("meeting-name.enabled", false)
            .setFeatureFlag("meeting-password.enabled", false)
            .setFeatureFlag("recording.enabled", false)
            .setFeatureFlag("recording.enabled", false)
            .setFeatureFlag("server-url-change.enabled", false)
            .build()

        joinConferenceView.visibility = View.GONE
        jitsiFrameLayout.visibility = View.VISIBLE

        jitsiMeetView.join(options)
        jitsiFrameLayout.addView(jitsiMeetView)
    }

    fun disposeJitsi() {
        jitsiMeetView?.dispose()
    }
}