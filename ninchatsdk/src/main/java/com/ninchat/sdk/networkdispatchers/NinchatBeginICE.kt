package com.ninchat.sdk.networkdispatchers

import com.ninchat.client.Props
import com.ninchat.client.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

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
        fun executeAsync(currentSession: Session?, callback: (actionId: Long) -> Long) {
            runBlocking {
                val actionId = execute(currentSession)
                callback(actionId)
            }
        }
    }
}