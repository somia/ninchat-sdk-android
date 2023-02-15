package com.ninchat.sdk.ninchatchatactivity.model

import com.ninchat.sdk.NinchatSessionManager

data class NinchatChatModel(
    var historyLoaded: Boolean = false,
    var toggleFullScreen: Boolean = false,
    var chatClosed: Boolean = false,
    var isGroupCall: Boolean = false,
) {
    fun parse() {
        isGroupCall = NinchatSessionManager.getInstance()?.ninchatSessionHolder?.isGroupVideo() ?: false
    }
}
