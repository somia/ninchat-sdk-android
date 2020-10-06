package com.ninchat.sdk.ninchatqueue.model

import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.helper.session.NinchatSessionManagerHelper

class NinchatQueueModel {
    var queueId: String? = null

    fun getQueueStatus(): String? {
        return NinchatSessionManagerHelper.getQueueStatus(queueId)
    }

    fun getInQueueMessageText(): String? {
        return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getInQueueMessageText()
    }

    fun getChatCloseText(): String? {
        return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getChatCloseText()
    }

    fun hasChannel(): Boolean {
        return NinchatSessionManager.getInstance()?.ninchatSessionHolder?.hasChannel() ?: false
    }

    companion object {
        @JvmField
        val REQUEST_CODE = NinchatQueueModel::class.java.hashCode() and 0xffff
        const val QUEUE_ID = "queueId"
    }
}