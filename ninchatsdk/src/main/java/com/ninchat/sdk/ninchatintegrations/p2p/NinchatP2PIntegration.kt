package com.ninchat.sdk.ninchatintegrations.p2p

import android.view.View
import com.ninchat.sdk.utils.messagetype.NinchatMessageTypes
import com.ninchat.sdk.views.NinchatWebRTCView
import kotlinx.android.synthetic.main.ninchat_video_view.view.*


class NinchatP2PIntegration(view: View, onToggleFullScreen: () -> Unit) {
    val webRTCView: NinchatWebRTCView by lazy {
        NinchatWebRTCView(view)

    }

    init {
        view.fullscreen_on_off.setOnClickListener { onToggleFullScreen() }
        view.audio_on_off.setOnClickListener { webRTCView.toggleAudio() }
        view.microphone_on_off.setOnClickListener { webRTCView.toggleMicrophone() }
        view.video_on_off.setOnClickListener { webRTCView.toggleVideo() }
        view.hangup_video.setOnClickListener { webRTCView.hangUp() }
    }

    fun handleRTCMessage(messageType: String?, payload: String?, onHandUp: () -> Unit) {
        webRTCView.handleWebRTCMessage(messageType, payload)
        if (NinchatMessageTypes.HANG_UP == messageType) {
            onHandUp()
        }
    }

    fun call() = webRTCView.call()
    fun hangUp() = webRTCView.hangUp()
    fun onPause() = webRTCView.onPause()
    fun onDestroy() = webRTCView.hangUp()
    fun onResume() = webRTCView.onResume()
}