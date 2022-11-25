package com.ninchat.sdk.utils.writingindicator

import android.os.Handler
import android.util.Log
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.networkdispatchers.NinchatUpdateMember
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class WritingIndicator() {
    private val inactiveTimeoutInMs = (30L * 1000)
    private var intervalInMs = 1L * 1000
    private var updateTextTask: Runnable? = null
    private var handler: Handler? = null
    private var lastWritingInMs = 0L
    private var lastMessageLength = 0
    private var wasWriting = false

    @JvmName("initiate")
    fun initiate() {
        updateTextTask = Runnable {
            val now = System.currentTimeMillis()
            // Time passed since we last type a character/message
            val timeElapseInMs = now - lastWritingInMs
            val isWriting = (lastMessageLength > 0 && timeElapseInMs < inactiveTimeoutInMs)
            notifyBackend(isWriting = isWriting)
            updateTextTask?.let { handler?.postDelayed(it, intervalInMs) }
        }
        handler = Handler()
        handler?.post(updateTextTask!!)
    }

    @JvmName("updateLastWritingTime")
    fun updateLastWritingTime(messageLength: Int) {
        lastMessageLength = messageLength
        lastWritingInMs = System.currentTimeMillis()
    }

    @JvmName("dispose")
    fun dispose() {
        updateTextTask?.let { handler?.removeCallbacks(it) }
        wasWriting = false
    }

    @JvmName("notifyBackend")
    fun notifyBackend(isWriting: Boolean) {
        // if there is no state change and it is not dirty
        if (isWriting == wasWriting) return
        wasWriting = isWriting
        NinchatSessionManager.getInstance()?.let { sessionManager ->
            NinchatScopeHandler.getIOScope().launch(CoroutineExceptionHandler(handler = { _, e ->
                Log.d("WritingIndicator", e.message ?: "")
            })) {
                NinchatUpdateMember.execute(
                        currentSession = sessionManager.session,
                        channelId = sessionManager.ninchatState.channelId,
                        userId = sessionManager.ninchatState.userId,
                        isWriting = isWriting,
                );
            }
        }

    }
}