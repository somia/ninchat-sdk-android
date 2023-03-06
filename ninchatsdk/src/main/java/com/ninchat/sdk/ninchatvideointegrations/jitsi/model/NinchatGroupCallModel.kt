package com.ninchat.sdk.ninchatvideointegrations.jitsi.model

import android.content.pm.ActivityInfo
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.managers.OrientationManager

data class NinchatGroupCallModel(
    var conferenceTitle: String = "",
    var conferenceButtonText: String = "",
    var conferenceDescription: String = "",
    var chatClosed: Boolean = false,
    var onGoingVideoCall: Boolean = false,
    var showChatView: Boolean = true,
    var softkeyboardVisible: Boolean = false,
    var currentOrientation: Int
) {

    fun parse() {
        conferenceTitle = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getConferenceTitleText() ?: ""
        conferenceButtonText = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getConferenceButtonText() ?: ""
        conferenceDescription = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getConferenceDescriptionText() ?: ""
    }

}