package com.ninchat.sdk.ninchatintegrations.jitsi

import android.util.Log
import android.widget.FrameLayout
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetView
import java.net.URL

class NinchatJitsiIntegration(var view: JitsiMeetView? = null) {
    var options: JitsiMeetConferenceOptions? = null

    fun dispose() {
        this.view?.leave()
    }

    // Current only one message type
    fun handleWebRTCMessage(
        jitsiVideoView: FrameLayout,
        serverAddress: String,
        serverAddressPrefix: String?,
        jitsiRoom: String?,
        jitsiToken: String?
    ) {
        val jitsiServerAddress = "https://${"jitsi-www"}.${serverAddress.removePrefix("api.")}"
        this.options = JitsiMeetConferenceOptions.Builder()
            .setServerURL(URL(jitsiServerAddress))
            .setRoom(jitsiRoom)
            .setToken(jitsiToken)
            .setFeatureFlag("overflow-menu.enabled", true)
            .setFeatureFlag("add-people.enabled", false)
            .setFeatureFlag("calendar.enabled", false)
            .setFeatureFlag("close-captions.enabled", false)
            .setFeatureFlag("chat.enabled", false)
            .setFeatureFlag("filmstrip.enabled", false)
            .setFeatureFlag("invite.enabled", false)
            .setFeatureFlag("kick-out.enabled", false)
            .setFeatureFlag("live-streaming.enabled", false)
            .setFeatureFlag("meeting-name.enabled", false)
            .setFeatureFlag("meeting-password.enabled", false)
            .setFeatureFlag("notifications.enabled", false)
            .setFeatureFlag("recording.enabled", false)
            .setFeatureFlag("welcomepage.enabled", false)
            .setFeatureFlag("video-share.enabled", false)
            .setFeatureFlag("toolbox.alwaysVisible", false)
            .setFeatureFlag("fullscreen.enabled'", true)
            .setFeatureFlag("help.enabled", false)
            .setFeatureFlag("lobby-mode.enabled", false)
            .setFeatureFlag("reactions.enabled", false)
            .build()

        val height = jitsiVideoView.measuredHeight
        val width = jitsiVideoView.measuredWidth
        view!!.join(options)
        jitsiVideoView.addView(view, height, width)
    }
}