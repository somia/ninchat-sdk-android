package com.ninchat.sdk

import com.ninchat.client.Props
import com.ninchat.client.Session
import com.ninchat.client.Strings
import kotlinx.coroutines.channels.SendChannel

class NinchatDescribeRealmQueues {
    companion object {
        suspend fun execute(currentSession: Session? = null,
                            channel: SendChannel<Long>,
                            realmId: String? = null,
                            audienceQueues: Collection<String>) {

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
                return channel.send(-1)
            }
            return channel.send(actionId)
        }
    }
}