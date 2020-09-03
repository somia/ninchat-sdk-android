package com.example.networkdispatcher

import com.ninchat.client.Props
import com.ninchat.client.Session

class NinchatDescribeFile {
    companion object {
        fun execute(currentSession: Session? = null, fileId: String? = null): Long {
            val params = Props()
            params.setString("action", "describe_file");
            fileId?.let {
                params.setString("file_id", fileId);
            }
            val actionId: Long = try {
                currentSession?.send(params, null) ?: -1
            } catch (e: Exception) {
                return -1
            }
            return actionId
        }
    }
}