package com.ninchat.sdk.ninchatintegrations.jitsi

import android.widget.FrameLayout
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetView
import java.net.URL

class NinchatJitsiIntegration(val view: JitsiMeetView? = null) {
    var options: JitsiMeetConferenceOptions? = null

    fun dispose() {
        this.view?.leave()
    }

    // Current only one message type
    fun handleWebRTCMessage(
        jitsiVideoView: FrameLayout,
        serverAddress: String,
        jitsiRoom: String?,
        jitsiToken: String?,
        width: Int,
        height: Int
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
            .setFeatureFlag("meeting-name.enabled", true)
            .setFeatureFlag("meeting-password.enabled", false)
            .setFeatureFlag("notifications.enabled", false)
            .setFeatureFlag("recording.enabled", false)
            .setFeatureFlag("welcomepage.enabled", true)
            .setFeatureFlag("video-share.enabled", false)
            .setFeatureFlag("toolbox.alwaysVisible", true)
            .setFeatureFlag("fullscreen.enabled'", true)
            .setFeatureFlag("help.enabled", false)
            .setFeatureFlag("lobby-mode.enabled", false)
            .setFeatureFlag("reactions.enabled", false)
            .build()

        view!!.join(options)
        jitsiVideoView.addView(view, width, height)
    }

    fun onDestroy() = view?.leave()
}