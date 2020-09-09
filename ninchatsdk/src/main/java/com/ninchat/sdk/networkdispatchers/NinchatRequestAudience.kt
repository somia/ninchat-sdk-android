package com.ninchat.sdk.networkdispatchers

import com.ninchat.client.Props
import com.ninchat.client.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NinchatRequestAudience {
    companion object {
        suspend fun execute(currentSession: Session? = null,
                            queueId: String? = null,
                            audienceMetadata: Props? = null): Long =
                withContext(Dispatchers.IO) {
                    val params = Props()
                    params.setString("action", "request_audience")
                    queueId.let {
                        params.setString("queue_id", queueId)
                    }
                    audienceMetadata?.let {
                        params.setObject("audience_metadata", audienceMetadata)
                    }
                    val actionId: Long = try {
                        currentSession?.send(params, null) ?: -1
                    } catch (e: Exception) {
                        -1
                    }
                    actionId
                }

        fun executeAsync(
                scope: CoroutineScope,
                currentSession: Session?,
                queueId: String? = null,
                audienceMetadata: Props? = null,
                callback: ((actionId: Long) -> Long)? = null) {
            scope.launch {
                val actionId = NinchatRequestAudience.execute(
                        currentSession = currentSession,
                        queueId = queueId,
                        audienceMetadata = audienceMetadata
                )
                callback?.let { it(actionId) }
            }
        }
    }
}