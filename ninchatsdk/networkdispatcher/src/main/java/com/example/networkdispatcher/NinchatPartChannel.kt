package com.example.networkdispatcher

import com.ninchat.client.Props
import com.ninchat.client.Session

class NinchatPartChannel {
    companion object {
        fun execute(currentSession: Session? = null, channelId: String? = null): Long {
            val params = Props()
            params.setString("action", "part_channel")
            channelId?.let {
                params.setString("channel_id", channelId)
            }
            val actionId: Long = try {
                currentSession?.send(params, null) ?: -1
            } catch (e: Exception) {
                return -1
            }
            return actionId
        }
    }
}