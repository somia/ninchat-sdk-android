package com.example.networkdispatcher

import com.ninchat.client.Payload
import com.ninchat.client.Props
import com.ninchat.client.Session
import com.ninchat.client.Strings

class NinchatSendPostAudienceQuestionnaire {
    fun execute(currentSession: Session? = null,
                channelId: String? = null,
                message: String? = null
    ): Long {

        val params = Props()
        params.setString("action", "send_message")
        params.setString("message_type", "ninchat.com/metadata")
        channelId?.let {
            params.setString("channel_id", channelId)
        }
        params.setBool("message_fold", true)

        val payload = Payload()
        message?.let{
            payload.append(message.toByteArray())
        }

        val actionId: Long = try {
            currentSession?.send(params, payload) ?: -1
        } catch (e: Exception) {
            return -1
        }
        return actionId
    }
}