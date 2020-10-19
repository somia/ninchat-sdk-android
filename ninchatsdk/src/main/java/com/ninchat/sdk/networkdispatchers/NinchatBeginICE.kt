package com.ninchat.sdk.networkdispatchers

import com.ninchat.client.Props
import com.ninchat.client.Session
import kotlinx.coroutines.*

class NinchatBeginICE {
    companion object {
        suspend fun execute(currentSession: Session? = null): Long =
                withContext(Dispatchers.IO) {
                    val params = Props()
                    params.setString("action", "begin_ice")
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
                callback: ((actionId: Long) -> Long)? = null) {
            scope.launch {
                val actionId = execute(currentSession)
                callback?.let { it(actionId) }
            }
        }
    }
}