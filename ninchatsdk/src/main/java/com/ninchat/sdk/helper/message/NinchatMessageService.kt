package com.ninchat.sdk.helper.message

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ninchat.client.Payload
import com.ninchat.client.Props
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.events.OnNewMessage
import com.ninchat.sdk.events.OnSubmitPostAudienceQuestionnaire
import com.ninchat.sdk.helper.propsparser.getSafe
import com.ninchat.sdk.ninchatmedia.model.NinchatFile
import com.ninchat.sdk.models.NinchatMessage
import com.ninchat.sdk.models.NinchatMessage.Type.MULTICHOICE
import com.ninchat.sdk.models.NinchatOption
import com.ninchat.sdk.networkdispatchers.NinchatDescribeFile
import com.ninchat.sdk.utils.messagetype.NinchatMessageTypes
import com.ninchat.sdk.utils.messagetype.NinchatMessageTypes.webrtcMessage
import com.ninchat.sdk.utils.misc.Broadcast
import com.ninchat.sdk.utils.misc.Misc.Companion.guessMimeTypeFromFileName
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler.getIOScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class NinchatMessageService {
    companion object {
        @JvmStatic
        fun handleIncomingMessage(params: Props?, payload: Payload?) {
            val sessionManager = NinchatSessionManager.getInstance()
            sessionManager?.let { ninchatSessionManager ->
                if (ninchatSessionManager.ninchatState?.channelId.isNullOrBlank()) {
                    return
                }
                if (ninchatSessionManager.ninchatState?.channelId != params?.getSafe<String>("channel_id")) {
                    return
                }
                val currentActionId = params?.getSafe<Long>("action_id")
                val messageType = params?.getSafe<String>("message_type")
                val sender = params?.getSafe<String>("message_user_id")
                val messageId = params?.getSafe<String>("message_id")
                val senderName = params?.getSafe<String>("message_user_name")
                val messageDeleted = params?.getSafe<Boolean>("message_deleted")
                val timestampMs = 1000L * ((params?.getSafe<Double>("message_time") ?: 0)).toLong()
                val builder = StringBuilder()
                payload?.let { currentPayload ->
                    for (i in 0 until currentPayload.length()) {
                        builder.append(String(currentPayload[i]))
                    }
                }

                if (webrtcMessage(messageType) && sender != ninchatSessionManager.ninchatState.userId) {
                    ninchatSessionManager.contextWeakReference?.get()?.let { mContext ->
                        LocalBroadcastManager.getInstance(mContext)
                                .sendBroadcast(Intent(Broadcast.WEBRTC_MESSAGE)
                                        .putExtra(Broadcast.WEBRTC_MESSAGE_ID, messageId)
                                        .putExtra(Broadcast.WEBRTC_MESSAGE_TYPE, messageType)
                                        .putExtra(Broadcast.WEBRTC_MESSAGE_SENDER, sender)
                                        .putExtra(Broadcast.WEBRTC_MESSAGE_CONTENT, builder.toString()))
                    }
                }

                Log.e("NinchatMessageService", "handleIncomingMessageUpdate: $messageType, $sender, $messageId, $messageDeleted")
                if (NinchatMessageTypes.UI_COMPOSE == messageType) {
                    try {
                        val messages = JSONArray(builder.toString())
                        val messageOptions = arrayListOf<NinchatOption>()
                        var simpleButtonChoice = false
                        for (j in 0 until messages.length()) {
                            val message = messages.getJSONObject(j)
                            val options = message.optJSONArray("options")
                            if (options != null) {
                                for (k in 0 until options.length()) {
                                    messageOptions.add(NinchatOption(options.getJSONObject(k)))
                                }
                                ninchatSessionManager.messageAdapter?.add(messageId, NinchatMessage(MULTICHOICE,
                                        sender,
                                        senderName,
                                        message.getString("label"),
                                        message,
                                        messageOptions,
                                        timestampMs))
                            } else {
                                simpleButtonChoice = true
                                messageOptions.add(NinchatOption(message))
                            }
                        }
                        if (simpleButtonChoice) {
                            ninchatSessionManager.messageAdapter?.add(messageId, NinchatMessage(MULTICHOICE,
                                    sender,
                                    senderName,
                                    null,
                                    null,
                                    messageOptions,
                                    timestampMs))
                        }
                        // A UI compose message
                        EventBus.getDefault().post(OnNewMessage())
                    } catch (_: Exception) {
                        // pass
                    }
                }

                if (ninchatSessionManager.ninchatState?.actionId == currentActionId) {
                    EventBus.getDefault().post(OnSubmitPostAudienceQuestionnaire())
                }
                if (messageType != NinchatMessageTypes.TEXT && messageType != NinchatMessageTypes.FILE) {
                    return
                }

                payload?.let { currentPayload ->
                    for (i in 0 until currentPayload.length()) {
                        JSONObject(String(currentPayload[i])).let { message ->
                            val files = message.optJSONArray("files")
                            if (files != null) {
                                files.optJSONObject(0)?.let { currentFile ->
                                    val filename = currentFile.optJSONObject("file_attrs")?.optString("name")
                                    val filesize = currentFile.optJSONObject("file_attrs")?.optInt("size")
                                            ?: 0
                                    var filetype = currentFile.optJSONObject("file_attrs")?.optString("type")
                                    if (filetype == null || filetype == "application/octet-stream") {
                                        filetype = guessMimeTypeFromFileName(filename)
                                    }
                                    val fileId = currentFile.optString("file_id")
                                    val ninchatFile = NinchatFile(messageId, fileId, filename, filesize, filetype, timestampMs, sender, senderName, sender != ninchatSessionManager.ninchatState?.userId)
                                    ninchatSessionManager.ninchatState?.addFile(fileId, ninchatFile)
                                    if (ninchatFile.url == null || ninchatFile.urlExpiry == null || ninchatFile.urlExpiry?.before(Date()) == true) {
                                        getIOScope().launch {
                                            NinchatDescribeFile.execute(
                                                    currentSession = ninchatSessionManager.session,
                                                    fileId = fileId
                                            )
                                        }
                                    }
                                }
                            } else {
                                ninchatSessionManager.messageAdapter?.add(messageId, NinchatMessage(message.getString("text"), null, sender, senderName, timestampMs, sender != ninchatSessionManager.ninchatState?.userId))
                            }
                        }
                    }
                    // file or text message
                    EventBus.getDefault().post(OnNewMessage())
                }
            }
        }

        @JvmStatic
        fun handleIncomingMessageUpdate(params: Props?, payload: Payload?) {
            val messageType = params?.getSafe<String>("message_type")
            val sender = params?.getSafe<String>("message_user_id")
            val messageId = params?.getSafe<String>("message_id")
            val messageDeleted = params?.getSafe<Boolean>("message_deleted")

            Log.e("NinchatMessageService", "handleIncomingMessageUpdate: $messageType, $sender, $messageId, $messageDeleted")
        }
    }
}