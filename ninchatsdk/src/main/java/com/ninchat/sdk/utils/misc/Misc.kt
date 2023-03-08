package com.ninchat.sdk.utils.misc

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.BuildConfig
import com.ninchat.sdk.adapters.NinchatMessageAdapter
import com.ninchat.sdk.helper.NinchatImageGetter
import com.ninchat.sdk.states.NinchatState

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
        fun toRichText(text: String?, mTextView: TextView?): Spanned {
            val centeredText = center(text)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mTextView != null) {
                mTextView.autoLinkMask = 0
                mTextView.movementMethod = LinkMovementMethod.getInstance()
                Html.fromHtml(centeredText, NinchatImageGetter(container = mTextView), null)
            } else {
                Html.fromHtml(centeredText)
            }
        }

        @JvmStatic
        fun guessMimeTypeFromFileName(name: String?): String {
            val extension = name?.replace(".*\\.".toRegex(), "")
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                ?: return "application/octet-stream"
        }

        @JvmStatic
        fun getNinchatChatBackground(context: Context, backgroundImage: Int): Drawable? {
            return ResourcesCompat.getDrawable(context.resources, backgroundImage, null)?.let {
                try {
                    return (it as BitmapDrawable).apply {
                        mutate()
                        setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                    }
                } catch (err: Exception) {
                    Log.e("NinchatSDK", err.message ?: "Error getting ninchat background")
                    null
                }
            }
        }

        @JvmStatic
        fun getFileName(uri: Uri, mContentResolver: ContentResolver): String {
            val cursor: Cursor = mContentResolver
                .query(uri, null, null, null, null, null) ?: return ""

            cursor.use {
                val nameIndex: Int = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (it.moveToFirst()) {
                    return it.getString(nameIndex)
                } else {
                    return ""
                }
            }
        }

        @JvmStatic
        fun shouldPartChannel(state: NinchatState?): Boolean {
            //1. there is no post audience questionnaire
            //2. Or if user click skipped rating
            return state?.let {
                it.skippedReview || !it.hasQuestionnaire(false)
            } ?: false
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
        const val JITSI_DISCOVERED_MESSAGE = BuildConfig.LIBRARY_PACKAGE_NAME + ".jitsiDiscoveredMessage"
        const val WEBRTC_MESSAGE_ID = WEBRTC_MESSAGE + ".messageId"
        const val WEBRTC_MESSAGE_SENDER = WEBRTC_MESSAGE + ".sender"
        const val WEBRTC_MESSAGE_TYPE = WEBRTC_MESSAGE + ".type"
        const val WEBRTC_MESSAGE_JITSI_ROOM = WEBRTC_MESSAGE + ".jitsi.room"
        const val WEBRTC_MESSAGE_JITSI_TOKEN = WEBRTC_MESSAGE + ".jitsi.token"
        const val WEBRTC_MESSAGE_JITSI_SERVER = WEBRTC_MESSAGE + ".jitsi.server"
        const val WEBRTC_MESSAGE_CONTENT = WEBRTC_MESSAGE + ".content"
        const val CLOSE_NINCHAT_ACTIVITY = BuildConfig.LIBRARY_PACKAGE_NAME + ".closeActivity"
    }
}

class Parameter {
    companion object {
        const val QUEUE_ID = "queueId"
        const val CHAT_IS_CLOSED = "isClosed"
    }
}

class NinchatLinearLayoutManager(context: Context) : LinearLayoutManager(context) {
    init {
        this.stackFromEnd = true
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (err: Error) {
            Log.e("NinchatLM", "${err.message}")
        }
    }
}

interface NinchatAdapterCallback {
    fun onMessageAdapter(adapter: NinchatMessageAdapter)
}