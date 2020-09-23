package com.ninchat.sdk.utils.misc

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.webkit.MimeTypeMap
import com.ninchat.sdk.BuildConfig

class Misc {
    companion object {
        const val NEW_SESSION = 0
        const val IN_QUEUE = 2
        const val HAS_CHANNEL = 3
        const val NONE = 4

        @JvmStatic
        fun center(text: String?): String {
            return text?.let {
                if (it.contains("<center>") && it.contains("</center>")) {
                    it
                } else {
                    "<center> $it </center>"
                }
            } ?: "<center> </center>"
        }

        @JvmStatic
        fun toSpanned(text: String?): Spanned {
            val centeredText = center(text)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(centeredText, Html.FROM_HTML_MODE_LEGACY) else Html.fromHtml(centeredText)
        }

        @JvmStatic
        fun guessMimeTypeFromFileName(name: String?): String {
            val extension = name?.replace(".*\\.".toRegex(), "")
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                    ?: return "application/octet-stream"
        }
    }
}

class Broadcast {
    companion object {
        const val QUEUE_UPDATED = BuildConfig.LIBRARY_PACKAGE_NAME + ".queueUpdated"
        const val AUDIENCE_ENQUEUED = BuildConfig.LIBRARY_PACKAGE_NAME + ".audienceEnqueued"
        const val CHANNEL_JOINED = BuildConfig.LIBRARY_PACKAGE_NAME + ".channelJoined"
        const val CHANNEL_CLOSED = BuildConfig.LIBRARY_PACKAGE_NAME + ".channelClosed"
        const val WEBRTC_MESSAGE = BuildConfig.LIBRARY_PACKAGE_NAME + ".webRTCMessage"
        const val WEBRTC_MESSAGE_ID = WEBRTC_MESSAGE + ".messageId"
        const val WEBRTC_MESSAGE_SENDER = WEBRTC_MESSAGE + ".sender"
        const val WEBRTC_MESSAGE_TYPE = WEBRTC_MESSAGE + ".type"
        const val WEBRTC_MESSAGE_CONTENT = WEBRTC_MESSAGE + ".content"
    }
}

class Parameter {
    companion object {
        const val QUEUE_ID = "queueId"
        const val CHAT_IS_CLOSED = "isClosed"
    }
}