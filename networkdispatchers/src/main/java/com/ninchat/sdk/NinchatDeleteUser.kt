package com.ninchat.sdk

import com.ninchat.client.Props
import com.ninchat.client.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NinchatDeleteUser {
    companion object {
        suspend fun execute(currentSession: Session? = null): Long = withContext(Dispatchers.IO) {
            val params = Props()
            params.setString("action", "delete_user")
            val actionId: Long = try {
                currentSession?.send(params, null) ?: -1
            } catch (e: Exception) {
                -1
            }
            actionId
        }
    }
}

