package com.ninchat.sdk.ninchatvideointegrations.jitsi.model

import com.ninchat.sdk.NinchatSessionManager

data class NinchatGroupCallModel(
    var conferenceTitle: String = "",
    var conferenceButtonText: String = "",
    var conferenceDescription: String = "",
    var endConference: Boolean = false
) {

    fun parse() {
        conferenceTitle = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getConferenceTitleText() ?: ""
        conferenceButtonText = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getConferenceButtonText() ?: ""
        conferenceDescription = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getConferenceDescriptionText() ?: ""
    }

    fun update(endConference: Boolean = false) {
        this.endConference = endConference
    }

}