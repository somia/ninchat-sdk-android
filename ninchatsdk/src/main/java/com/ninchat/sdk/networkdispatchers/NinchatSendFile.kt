package com.ninchat.sdk.networkdispatchers

import com.ninchat.client.Payload
import com.ninchat.client.Props
import com.ninchat.client.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NinchatSendFile {
    companion object {
        suspend fun execute(currentSession: Session? = null,
                            channelId: String? = null,
                            fileName: String? = null,
                            data: ByteArray? = null): Long =
                withContext(Dispatchers.IO) {
                    val params = Props()
                    params.setString("action", "send_file")
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
                        -1
                    }
                    actionId
                }

        @JvmStatic
        fun executeAsync(
                scope: CoroutineScope,
                currentSession: Session? = null,
                channelId: String? = null,
                fileName: String? = null,
                data: ByteArray? = null,
                callback: ((actionId: Long) -> Long)? = null) {
            scope.launch {
                val actionId = NinchatSendFile.execute(
                        currentSession = currentSession,
                        channelId = channelId,
                        fileName = fileName,
                        data = data
                )
                callback?.let { it(actionId) }
            }
        }
    }
}