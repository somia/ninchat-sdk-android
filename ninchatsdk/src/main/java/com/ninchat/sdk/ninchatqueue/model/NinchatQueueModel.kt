package com.ninchat.sdk.ninchatqueue.model

import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.helper.session.NinchatSessionManagerHelper

class NinchatQueueModel {
    var queueId: String? = null

    fun getQueueStatus(): String? {
        return if (isClosedQueue()) {
            getQueueCloseText()
        } else
            NinchatSessionManagerHelper.getQueueStatus(queueId)
    }

    fun isClosedQueue(): Boolean {
        val currentQueue = NinchatSessionManager.getInstance()?.ninchatState?.getQueueList()?.find { currentQueue -> currentQueue.id == queueId }
        val isInQueue = NinchatSessionManager.getInstance().ninchatSessionHolder?.isInQueue()
                ?: false
        return when {
            isInQueue -> false
            else ->
                currentQueue?.isClosed
                        ?: false
        }
    }

    fun getInQueueMessageText(): String? {
        return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getInQueueMessageText()
    }

    fun getQueueCloseText(): String? {
        val currentQueue = NinchatSessionManager.getInstance()?.ninchatState?.getQueueList()?.find { currentQueue -> currentQueue.id == queueId }
        val text = NinchatSessionManager.getInstance().ninchatState?.siteConfig?.getQueueName(
                name = currentQueue?.name ?: "",
                closed = true
        )
        return text
    }

    fun getChatCloseText(): String? {
        return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getChatCloseText()
    }

    fun hasChannel(): Boolean {
        return NinchatSessionManager.getInstance()?.ninchatSessionHolder?.hasChannel() ?: false
    }

    fun isAlreadyInQueueList(): Boolean {
        return NinchatSessionManager.getInstance()?.ninchatState?.getQueueList()?.any { currentQueue -> currentQueue.id == queueId }
                ?: false
    }

    companion object {
        @JvmField
        val REQUEST_CODE = NinchatQueueModel::class.java.hashCode() and 0xffff
        const val QUEUE_ID = "queueId"
    }
}