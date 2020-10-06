package com.ninchat.sdk.ninchatqueue.model

import com.ninchat.sdk.activities.NinchatQueueActivity

class NinchatQueueModel {
    companion object {
        @JvmField
        val REQUEST_CODE = NinchatQueueModel::class.java.hashCode() and 0xffff
        const val QUEUE_ID = "queueId"
    }
}