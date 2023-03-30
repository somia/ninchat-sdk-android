package com.ninchat.sdk.utils.messagetype

object NinchatMessageTypes {
    const val TEXT = "ninchat.com/text"
    const val FILE = "ninchat.com/file"
    const val UI_COMPOSE = "ninchat.com/ui/compose"
    const val UI_ACTION = "ninchat.com/ui/action"
    const val METADATA = "ninchat.com/metadata"
    const val DELETED = "ninchat.com/deleted"
    const val ICE_CANDIDATE = "ninchat.com/rtc/ice-candidate"
    const val ANSWER = "ninchat.com/rtc/answer"
    const val OFFER = "ninchat.com/rtc/offer"
    const val CALL = "ninchat.com/rtc/call"
    const val PICK_UP = "ninchat.com/rtc/pick-up"
    const val HANG_UP = "ninchat.com/rtc/hang-up"
    const val WEBRTC_SERVERS_PARSED = "ninchat.com/rtc/serversParsed"
    val WEBRTC_MESSAGE_TYPES = listOf<String>(
            ICE_CANDIDATE,
            ANSWER,
            OFFER,
            CALL,
            PICK_UP,
            HANG_UP,
    )

    @JvmStatic
    fun webrtcMessage(messageType: String?): Boolean {
        messageType?.let {
            val contains = messageType in WEBRTC_MESSAGE_TYPES
            return contains
        }
        return false
    }
}