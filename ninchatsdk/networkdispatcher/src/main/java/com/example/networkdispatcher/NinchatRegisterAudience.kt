package com.example.networkdispatcher

import com.ninchat.client.Props
import com.ninchat.client.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NinchatRegisterAudience {
    companion object {
        suspend fun execute(currentSession: Session? = null,
                            queueId: String? = null,
                            audienceMetadata: Props? = null): Long =
                withContext(Dispatchers.IO) {
                    val params = Props()
                    params.setString("action", "register_audience")
                    val actionId: Long = if (!queueId.isNullOrEmpty() && audienceMetadata != null) {
                        params.setString("queue_id", queueId)
                        params.setObject("audience_metadata", audienceMetadata)
                        try {
                            currentSession?.send(params, null) ?: -1L
                        } catch (e: Exception) {
                            -1L
                        }
                    } else {
                        -1L
                    }
                    actionId
                }
    }
}