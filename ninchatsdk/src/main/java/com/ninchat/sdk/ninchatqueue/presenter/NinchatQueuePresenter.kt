package com.ninchat.sdk.ninchatqueue.presenter

import android.content.Context
import android.content.Intent
import com.ninchat.sdk.activities.NinchatQueueActivity
import com.ninchat.sdk.ninchatqueue.model.NinchatQueueModel

class NinchatQueuePresenter {
    companion object {
        @JvmStatic
        fun getLaunchIntent(context: Context?, queueId: String?): Intent {
            return Intent(context, NinchatQueueActivity::class.java).putExtra(NinchatQueueModel.QUEUE_ID, queueId)
        }
    }
}