package com.example.networkdispatcher

import com.ninchat.client.Props
import com.ninchat.client.Session

class NinchatUpdateMember {
    fun execute(currentSession: Session? = null,
                channelId: String? = null,
                userId: String? = null,
                isWriting: Boolean = false): Long {

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
            return -1
        }
        return actionId
    }
}