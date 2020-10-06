package com.ninchat.sdk.ninchatqueue.view

import android.os.Bundle
import android.view.View
import com.ninchat.sdk.R
import com.ninchat.sdk.activities.NinchatBaseActivity

class NinchatQueueActivity : NinchatBaseActivity() {
    override val layoutRes: Int
        get() = R.layout.activity_ninchat_queue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun onClose(view: View) {
        setResult(RESULT_CANCELED, null)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}