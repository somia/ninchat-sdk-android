package com.ninchat.sdk.ninchatvideointegrations.jitsi.model

import com.ninchat.sdk.NinchatSessionManager

data class NinchatGroupCallModel(
    var conferenceTitle: String = "",
    var conferenceButtonText: String = "",
    var conferenceDescription: String = "",
    var chatClosed: Boolean = false,
    var onGoingVideoCall: Boolean = false,
    var showChatView: Boolean = true
) {

    fun parse() {
        conferenceTitle = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getConferenceTitleText() ?: ""
        conferenceButtonText = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getConferenceButtonText() ?: ""
        conferenceDescription = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getConferenceDescriptionText() ?: ""
    }

}