package com.example.networkdispatcher

import com.ninchat.client.Props
import com.ninchat.client.Session

class NinchatDeleteUser {
    companion object {
        fun execute(currentSession: Session? = null): Long {
            val params = Props()
            params.setString("action", "delete_user")
            val actionId: Long = try {
                currentSession?.send(params, null) ?: -1
            } catch (e: Exception) {
                return -1
            }
            return actionId
        }
    }
}

