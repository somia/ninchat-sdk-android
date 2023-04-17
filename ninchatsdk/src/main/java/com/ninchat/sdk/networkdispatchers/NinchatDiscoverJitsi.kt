package com.ninchat.sdk.networkdispatchers

import android.util.Log
import com.ninchat.client.Props
import com.ninchat.client.Session
import kotlinx.coroutines.*

class NinchatDiscoverJitsi {
    companion object {
        suspend fun execute(currentSession: Session? = null,
                            channelId: String? = null): Long =
                withContext(Dispatchers.IO) {
                    val params = Props()
                    params.setString("action", "discover_jitsi")
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
            scope.launch(CoroutineExceptionHandler { _, exception -> Log.d("NinchatDiscoverJitsi", exception.localizedMessage
                    ?: "") }) {
                val actionId = execute(
                        currentSession = currentSession,
                        channelId = channelId
                )
                callback?.let { it(actionId) }
            }
        }
    }
}