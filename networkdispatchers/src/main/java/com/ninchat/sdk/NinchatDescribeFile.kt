package com.ninchat.sdk

import com.ninchat.client.Props
import com.ninchat.client.Session
import kotlinx.coroutines.channels.SendChannel

class NinchatDescribeFile {
    companion object {
        suspend fun execute(currentSession: Session? = null,
                            channel: SendChannel<Long>,
                            fileId: String? = null) {
            val params = Props()
            params.setString("action", "describe_file");
            fileId?.let {
                params.setString("file_id", fileId);
            }
            val actionId: Long = try {
                currentSession?.send(params, null) ?: -1
            } catch (e: Exception) {
                return channel.send(-1)
            }
            return channel.send(actionId)
        }
    }
}