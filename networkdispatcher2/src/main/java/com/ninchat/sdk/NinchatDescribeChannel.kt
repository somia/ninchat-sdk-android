package com.ninchat.sdk

import com.ninchat.client.Props
import com.ninchat.client.Session
import kotlinx.coroutines.channels.SendChannel

class NinchatDescribeChannel {
    companion object {
        suspend fun execute(currentSession: Session? = null,
                            channel: SendChannel<Long>,
                            channelId: String? = null) {
            val params = Props()
            params.setString("action", "describe_channel")
            channelId?.let {
                params.setString("channel_id", channelId)
            }
            val actionId: Long = try {
                currentSession?.send(params, null) ?: -1
            } catch (e: Exception) {
                return channel.send(-1)
            }
            return channel.send(actionId)
        }
    }
}