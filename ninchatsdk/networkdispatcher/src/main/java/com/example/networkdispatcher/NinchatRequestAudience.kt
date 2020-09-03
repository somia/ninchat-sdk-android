package com.example.networkdispatcher

import com.ninchat.client.Props
import com.ninchat.client.Session

class NinchatRequestAudience {
    fun execute(currentSession: Session? = null,
                queueId: String? = null,
                audienceMetadata: Props? = null): Long {
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
            return -1
        }
        return actionId
    }
}