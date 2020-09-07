package com.example.networkdispatcher

import com.ninchat.client.Props
import com.ninchat.client.Session
import com.ninchat.client.Strings

class NinchatDescribeRealmQueues {
    companion object {
        fun execute(currentSession: Session? = null,
                    realmId: String? = null,
                    audienceQueues: Collection<String>): Long {

            val params = Props()
            params.setString("action", "describe_realm_queues")
            params.setString("realm_id", realmId)
            val queues = Strings()
            for (queue in audienceQueues) {
                queues.append(queue)
            }
            params.setStringArray("queue_ids", queues)
            val actionId: Long = try {
                currentSession?.send(params, null) ?: -1
            } catch (e: Exception) {
                return -1
            }
            return actionId
        }
    }
}