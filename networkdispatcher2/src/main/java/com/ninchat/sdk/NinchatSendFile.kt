package com.ninchat.sdk

import com.ninchat.client.Payload
import com.ninchat.client.Props
import com.ninchat.client.Session

class NinchatSendFile {
    fun execute(currentSession: Session? = null,
                channelId: String? = null,
                fileName: String? = null,
                data: ByteArray? = null): Long {
        val params = Props()
        params.setString("action", "request_audience")
        channelId.let {
            params.setString("channel_id", channelId)
        }

        val fileAttrs = Props()
        fileName?.let {
            fileAttrs.setString("name", fileName)
        }
        params.setObject("file_attrs", fileAttrs)

        val payload = Payload()
        data?.let {
            payload.append(data)
        }
        
        val actionId: Long = try {
            currentSession?.send(params, payload) ?: -1
        } catch (e: Exception) {
            return -1
        }
        return actionId

    }
}