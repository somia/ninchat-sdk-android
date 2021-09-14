package com.ninchat.sdk.ninchatchatactivity.model

data class LayoutModel(
    var chatClosed: Boolean = false,
    val sendButtonText: String = "",
    val showSendButtonText: Boolean = true,
    val showSendButtonIcon: Boolean = false,
    val showAttachment: Boolean = false,
    val showVideoCalls: Boolean = false,
    val showTitlebar: Boolean = false,
    val showRatingView: Boolean = false,
    val chatCloseText: String = "",
    val chatCloseConfirmationText: String = "",
    val chatCloseDeclineText: String = ""

) {
}