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
    private lateinit var updateTextTask: Runnable
    private lateinit var handler: Handler
    private var lastWritingInMs = 0L
    private var lastMessageLength = 0
    private var wasWriting = false
    private var dirty = false // special flag that handles "server stops writing indication" ( since we don't catch server fired stop writing message. require for resume writing indication )

    @JvmName("initiate")
    fun initiate() {
        updateTextTask = Runnable {
            val now = System.currentTimeMillis()
            // Time passed since we last type a character/message
            val timeElapseInMs = now - lastWritingInMs
            val isWriting = (lastMessageLength > 0 && timeElapseInMs < inactiveTimeoutInMs)
            notifyBackend(isWriting = isWriting)
            handler.postDelayed(updateTextTask, intervalInMs)
        }
        dirty = false
        handler = Handler()
        handler.post(updateTextTask)
    }

    @JvmName("updateLastWritingTime")
    fun updateLastWritingTime(messageLength: Int) {
        // is it after 30 second ?
        val now = System.currentTimeMillis()
        lastMessageLength = messageLength
        dirty = now - lastWritingInMs > inactiveTimeoutInMs
        lastWritingInMs = now
    }

    @JvmName("dispose")
    fun dispose() {
        handler.removeCallbacks(updateTextTask)
        dirty = false
        wasWriting = false
    }

    private fun notifyBackend(isWriting: Boolean) {
        // if there is no state change and it is not dirty
        if (isWriting == wasWriting && !dirty) return
        wasWriting = isWriting
        dirty = false
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