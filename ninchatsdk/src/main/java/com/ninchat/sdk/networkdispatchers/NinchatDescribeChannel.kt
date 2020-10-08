package com.ninchat.sdk.networkdispatchers

import com.ninchat.client.Props
import com.ninchat.client.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NinchatDescribeChannel {
    companion object {
        suspend fun execute(currentSession: Session? = null,
                            channelId: String? = null): Long =
                withContext(Dispatchers.IO) {
                    val params = Props()
                    params.setString("action", "describe_channel")
                    channelId?.let {
                        params.setString("channel_id", channelId)
                    }
                    val actionId: Long = try {
                        currentSession?.send(params, null) ?: -1
                    } catch (e: Exception) {
                        -1
                    }
                    actionId
                }

        @JvmStatic
        fun executeAsync(
                scope: CoroutineScope,
                currentSession: Session?,
                channelId: String? = null,
                callback: ((actionId: Long) -> Long)? = null) {
            scope.launch {
                val actionId = NinchatDescribeChannel.execute(
                        currentSession=currentSession,
                        channelId = channelId
                )
                callback?.let { it(actionId) }
            }
        }
    }
}