package com.ninchat.sdk.networkdispatchers

import com.ninchat.client.Payload
import com.ninchat.client.Props
import com.ninchat.client.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NinchatSendPostAudienceQuestionnaire {
    companion object {
        suspend fun execute(currentSession: Session? = null,
                            channelId: String? = null,
                            message: String? = null
        ): Long =
                withContext(Dispatchers.IO) {

                    val params = Props()
                    params.setString("action", "send_message")
                    params.setString("message_type", "ninchat.com/metadata")
                    channelId?.let {
                        params.setString("channel_id", channelId)
                    }
                    params.setBool("message_fold", true)

                    val payload = Payload()
                    message?.let {
                        payload.append(message.toByteArray())
                    }

                    val actionId: Long = try {
                        currentSession?.send(params, payload) ?: -1
                    } catch (e: Exception) {
                        -1
                    }
                    actionId
                }

        @JvmStatic
        fun executeAsync(
                scope: CoroutineScope,
                currentSession: Session? = null,
                channelId: String? = null,
                message: String? = null,
                callback: ((actionId: Long) -> Long)? = null) {
            scope.launch {
                val actionId = NinchatSendPostAudienceQuestionnaire.execute(
                        currentSession = currentSession,
                        channelId = channelId,
                        message = message
                )
                callback?.let { it(actionId) }
            }
        }
    }
}