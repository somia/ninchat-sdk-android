package com.ninchat.sdk.networkdispatchers

import com.ninchat.client.Props
import com.ninchat.client.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NinchatDescribeQueue {
    companion object {
        suspend fun execute(currentSession: Session? = null,
                            queueId: String? = null): Long =
                withContext(Dispatchers.IO) {
                    val params = Props()
                    params.setString("action", "describe_queue")
                    queueId?.let {
                        params.setString("queue_id", queueId)
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
                queueId: String? = null,
                callback: ((actionId: Long) -> Long)? = null) {
            scope.launch {
                val actionId = NinchatDescribeQueue.execute(
                        currentSession=currentSession,
                        queueId = queueId
                )
                callback?.let { it(actionId) }
            }
        }
    }
}