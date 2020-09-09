package com.ninchat.sdk.networkdispatchers

import com.ninchat.client.Props
import com.ninchat.client.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NinchatDescribeFile {
    companion object {
        suspend fun execute(currentSession: Session? = null,
                            fileId: String? = null): Long =
                withContext(Dispatchers.IO) {
                    val params = Props()
                    params.setString("action", "describe_file");
                    val actionId: Long = if (!fileId.isNullOrEmpty()) {
                        params.setString("file_id", fileId);
                        try {
                            currentSession?.send(params, null) ?: -1
                        } catch (e: Exception) {
                            -1
                        }
                    } else {
                        -1
                    }
                    actionId
                }

        @JvmStatic
        fun executeAsync(
                scope: CoroutineScope,
                currentSession: Session?,
                fileId: String? = null,
                callback: ((actionId: Long) -> Long)? = null) {
            scope.launch {
                val actionId = NinchatDescribeFile.execute(
                        currentSession = currentSession,
                        fileId = fileId)
                callback?.let { it(actionId) }
            }
        }
    }
}