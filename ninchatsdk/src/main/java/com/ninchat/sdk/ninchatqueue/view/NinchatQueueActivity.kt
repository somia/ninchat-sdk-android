package com.ninchat.sdk.ninchatqueue.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.ninchat.sdk.R
import com.ninchat.sdk.activities.NinchatBaseActivity
import com.ninchat.sdk.ninchatchatactivity.presenter.NinchatChatPresenter
import com.ninchat.sdk.ninchatqueue.model.NinchatQueueModel
import com.ninchat.sdk.ninchatqueue.presenter.INinchatQueuePresenter
import com.ninchat.sdk.ninchatqueue.presenter.NinchatQueuePresenter
import com.ninchat.sdk.utils.misc.Parameter
import kotlinx.android.synthetic.main.activity_ninchat_queue.*
import kotlinx.android.synthetic.main.activity_ninchat_queue.view.*

class NinchatQueueActivity : NinchatBaseActivity(), INinchatQueuePresenter {
    var queueId: String? = null

    override val layoutRes: Int
        get() = R.layout.activity_ninchat_queue

    // ninchat queue presenter
    private val ninchatQueuePresenter = NinchatQueuePresenter(
        ninchatQueueModel = NinchatQueueModel(),
        callback = this,
        mContext = this
    )

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

        // 2: show queue animation
        // do not show animation during testing
        // https://stackoverflow.com/questions/29550508/espresso-freezing-on-view-with-looping-animation
        if (intent?.getBooleanExtra("isDebug", false) == false) {
            ninchatQueuePresenter.showQueueAnimation(ninchat_queue_activity_progress)
        }
        //3: update queue view
        ninchatQueuePresenter.updateQueueView(ninchat_queue_activity)

        //4: add actions
        ninchatQueuePresenter.mayBeAttachTitlebar(ninchat_queue_activity, callback = {
            this.onClose(ninchat_queue_activity)
        })

        //5: subscriber broadcaster
        ninchatQueuePresenter.subscribeBroadcaster()
        //6: try to join the queue
        ninchatQueuePresenter.mayBeJoinQueue()

    }

    override fun onDestroy() {
        ninchatQueuePresenter.unSubscribeBroadcaster()
        super.onDestroy()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == NinchatChatPresenter.REQUEST_CODE) {
            // check data is null or not. Can through exception
            val currentQueueId = data?.getStringExtra(Parameter.QUEUE_ID)
            if (currentQueueId.isNullOrEmpty()) {
                setResult(RESULT_OK, data)
                finish()
                return
            }
            ninchatQueuePresenter.updateQueueId(intent = data)
            ninchatQueuePresenter.updateQueueView(ninchat_queue_activity)

            ninchatQueuePresenter.mayBeAttachTitlebar(
                ninchat_queue_activity, callback = {
                this.onClose(ninchat_queue_activity)
            })
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun onClose(view: View) {
        ninchatQueuePresenter.mayBeDeleteUser()
        setResult(RESULT_CANCELED, null)
        finish()
    }

    override fun onChannelJoined(isClosed: Boolean) {
        val intent =
            NinchatQueuePresenter.getLaunchIntentForChatActivity(applicationContext, isClosed)
        startActivityForResult(intent, NinchatChatPresenter.REQUEST_CODE)
    }

    override fun onQueueUpdate() {
        ninchatQueuePresenter.updateQueueView(ninchat_queue_activity)
        ninchatQueuePresenter.mayBeAttachTitlebar(ninchat_queue_activity, callback = {
            this.onClose(ninchat_queue_activity)
        })
    }
}