package com.ninchat.sdk.networkdispatchers

import com.ninchat.client.Props
import com.ninchat.client.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NinchatPartChannel {
    companion object {
        suspend fun execute(currentSession: Session? = null,
                            channelId: String? = null): Long =
                withContext(Dispatchers.IO) {
                    val params = Props()
                    params.setString("action", "part_channel")
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
                channelId: String?,
                callback: ((actionId: Long) -> Long)? = null) {
            scope.launch {
                val actionId = NinchatPartChannel.execute(
                        currentSession = currentSession,
                        channelId = channelId
                )
                callback?.let { it(actionId) }
            }
        }
    }
}