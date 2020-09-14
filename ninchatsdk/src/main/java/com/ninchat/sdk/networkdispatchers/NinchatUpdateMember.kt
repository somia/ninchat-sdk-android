package com.ninchat.sdk.networkdispatchers

import com.ninchat.client.Props
import com.ninchat.client.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NinchatUpdateMember {
    companion object {
        suspend fun execute(currentSession: Session? = null,
                            channelId: String? = null,
                            userId: String? = null,
                            isWriting: Boolean = false): Long =
                withContext(Dispatchers.IO) {

                    val memberAttrs = Props()
                    memberAttrs.setBool("writing", isWriting)

                    val params = Props()
                    params.setString("action", "update_member")
                    channelId?.let {
                        params.setString("channel_id", channelId)
                    }
                    userId?.let {
                        params.setString("user_id", userId)
                    }
                    params.setObject("member_attrs", memberAttrs)
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
                channelId: String? = null,
                userId: String? = null,
                isWriting: Boolean = false,
                callback: ((actionId: Long) -> Long)? = null) {
            scope.launch {
                val actionId = NinchatUpdateMember.execute(
                        currentSession = currentSession,
                        channelId = channelId,
                        userId = userId,
                        isWriting = isWriting
                )
                callback?.let { it(actionId) }
            }
        }
    }
}