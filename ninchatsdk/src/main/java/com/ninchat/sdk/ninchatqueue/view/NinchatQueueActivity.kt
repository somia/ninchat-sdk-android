package com.ninchat.sdk.ninchatqueue.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.ninchat.sdk.R
import com.ninchat.sdk.activities.NinchatBaseActivity
import com.ninchat.sdk.activities.NinchatChatActivity
import com.ninchat.sdk.ninchatqueue.model.NinchatQueueModel
import com.ninchat.sdk.ninchatqueue.presenter.INinchatQueuePresenter
import com.ninchat.sdk.ninchatqueue.presenter.NinchatQueuePresenter
import com.ninchat.sdk.utils.misc.Parameter
import kotlinx.android.synthetic.main.activity_ninchat_queue.*

class NinchatQueueActivity : NinchatBaseActivity(), INinchatQueuePresenter {
    var queueId: String? = null

    override val layoutRes: Int
        get() = R.layout.activity_ninchat_queue

    // ninchat queue presenter
    private val ninchatQueuePresenter = NinchatQueuePresenter(
            ninchatQueueModel = NinchatQueueModel(),
            callback = this,
            mContext = this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If the app is killed in the background sessionManager is not initialized the SDK must
        // be exited and the NinchatSession needs to be initialized again
        if (!ninchatQueuePresenter.hasSession()) {
            setResult(RESULT_CANCELED, null)
            finish()
            overridePendingTransition(0, 0)
            return
        }
        //1: update the queue id
        ninchatQueuePresenter.updateQueueId(intent = intent)
        //2: show queue animation
        ninchatQueuePresenter.showQueueAnimation(ninchat_queue_activity_progress)
        //3: update queue view
        ninchatQueuePresenter.updateQueueView(ninchat_queue_activity)
        //4: subscriber broadcaster
        ninchatQueuePresenter.subscribeBroadcaster()
        //5: try to join the queue
        ninchatQueuePresenter.mayBeJoinQueue()
    }

    override fun onDestroy() {
        ninchatQueuePresenter.unSubscribeBroadcaster()
        super.onDestroy()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == NinchatChatActivity.REQUEST_CODE) {
            // check data is null or not. Can through exception
            val currentQueueId = data?.getStringExtra(Parameter.QUEUE_ID)
            if (currentQueueId.isNullOrEmpty()) {
                setResult(RESULT_OK, data)
                finish()
                return
            }
            ninchatQueuePresenter.updateQueueId(intent = data)
            ninchatQueuePresenter.updateQueueView(ninchat_queue_activity)
        }
    }

    fun onClose(view: View) {
        ninchatQueuePresenter.closeView()
        setResult(RESULT_CANCELED, null)
        finish()
    }

    override fun onChannelJoined(isClosed: Boolean) {
        val intent = NinchatQueuePresenter.getLaunchIntentForChatActivity(applicationContext, isClosed)
        startActivityForResult(intent, NinchatChatActivity.REQUEST_CODE)
    }

    override fun onQueueUpdate() {
        ninchatQueuePresenter.updateQueueView(ninchat_queue_activity)
    }
}