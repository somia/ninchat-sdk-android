package com.ninchat.sdk.networkdispatchers

import com.ninchat.client.Props
import com.ninchat.client.Session
import com.ninchat.client.Strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NinchatDescribeRealmQueues {
    companion object {
        suspend fun execute(currentSession: Session? = null,
                            realmId: String? = null,
                            audienceQueues: Collection<String>? = null): Long =
                withContext(Dispatchers.IO) {

                    val params = Props()
                    params.setString("action", "describe_realm_queues")
                    params.setString("realm_id", realmId)
                    val queues = Strings()
                    if (audienceQueues != null) {
                        for (queue in audienceQueues) {
                            queues.append(queue)
                        }
                    }
                    params.setStringArray("queue_ids", queues)
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
                currentSession: Session? = null,
                realmId: String? = null,
                audienceQueues: Collection<String>? = null,
                callback: ((actionId: Long) -> Long)? = null) {
            scope.launch {
                val actionId = NinchatDescribeRealmQueues.execute(
                        currentSession = currentSession,
                        realmId = realmId,
                        audienceQueues = audienceQueues)
                callback?.let { it(actionId) }
            }
        }
    }
}