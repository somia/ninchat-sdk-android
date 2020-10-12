package com.ninchat.sdk.ninchatactivity.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.activities.NinchatBaseActivity
import com.ninchat.sdk.ninchatquestionnaire.view.NinchatQuestionnaireActivity
import com.ninchat.sdk.ninchatactivity.model.NinchatActivityModel
import com.ninchat.sdk.ninchatactivity.presenter.INinchatActivityPresenter
import com.ninchat.sdk.ninchatactivity.presenter.NinchatActivityPresenter
import com.ninchat.sdk.ninchatquestionnaire.model.NinchatQuestionnaireModel
import com.ninchat.sdk.ninchatquestionnaire.presenter.NinchatQuestionnairePresenter
import com.ninchat.sdk.ninchatqueue.model.NinchatQueueModel
import kotlinx.android.synthetic.main.activity_ninchat.*

class NinchatActivity : NinchatBaseActivity(), INinchatActivityPresenter {
    private val ninchatActivityPresenter = NinchatActivityPresenter(
            ninchatActivityModel = NinchatActivityModel(),
            iNinchatActivityPresenter = this,
            mContext = this)

    override val layoutRes: Int
        get() = R.layout.activity_ninchat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If the app is killed in the background sessionManager is not initialized the SDK must
        // be exited and the NinchatSession needs to be initialized again
        if (!ninchatActivityPresenter.hasSession()) {
            // Use a small delay before transition for UX purposes. Without the delay the app looks
            // like it's crashing since there can be 3 activities that will be finished.
            Handler().postDelayed({
                setResult(RESULT_CANCELED, null)
                finish()
                // Use a slide transition just to minimize the impression that the app has crashed
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }, NinchatActivityModel.TRANSITION_DELAY)
            return
        }

        //1: update queue id
        ninchatActivityPresenter.updateQueueId(intent = intent)
        //2: register listeners
        ninchatActivityPresenter.subscribeBroadcaster()
        //3: update view
        ninchatActivityPresenter.updateActivityView(
                topHeader = ninchat_activity_header,
                closeButton = ninchat_activity_close,
                motD = ninchat_activity_motd,
                noQueue = ninchat_activity_no_queues
        )
        //4: if has a queue then try to open questionnaire or queue activity
        if (ninchatActivityPresenter.hasQueue()) {
            // try to open questionnaire or queue activity
            if (ninchatActivityPresenter.shouldOpenPreAudienceQuestionnaireActivity()) {
                ninchatActivityPresenter.openQuestionnaireActivity(this, queueId = ninchatActivityPresenter.ninchatActivityModel.queueId)
            } else {
                ninchatActivityPresenter.openQueueActivity(this, queueId = ninchatActivityPresenter.ninchatActivityModel.queueId)
            }
        }

        //5: set queue adapter
        ninchatActivityPresenter.setQueueAdapter(
                recyclerView = ninchat_activity_queue_list,
                mActivity = this,
                closeButton = ninchat_activity_close,
                motD = ninchat_activity_motd,
                noQueue = ninchat_activity_no_queues
        )
    }

    override fun onDestroy() {
        ninchatActivityPresenter.unSubscribeBroadcaster()
        super.onDestroy()
    }

    fun onCloseClick(view: View?) {
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == NinchatQueueModel.REQUEST_CODE ->
                if (resultCode == RESULT_OK || !ninchatActivityPresenter.ninchatActivityModel.queueId.isNullOrEmpty()) {
                    if (resultCode == RESULT_OK && ninchatActivityPresenter.shouldOpenPostAudienceQuestionnaireActivity()) {
                        ninchatActivityPresenter.openQuestionnaireActivity(this, ninchatActivityPresenter.ninchatActivityModel.queueId)
                    } else {
                        NinchatSessionManager.getInstance()?.close()
                        setResult(resultCode, data)
                        finish()
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    finish()
                }

            requestCode == NinchatQuestionnairePresenter.REQUEST_CODE ->
                if (resultCode == RESULT_OK) {
                    setResult(resultCode, data);
                    val openQueue = data?.getBooleanExtra(NinchatQuestionnaireModel.OPEN_QUEUE, false)
                            ?: false
                    val newQueueId = data?.getStringExtra(NinchatActivityModel.QUEUE_ID)
                    if (openQueue && !newQueueId.isNullOrEmpty()) {
                        ninchatActivityPresenter.updateQueueId(intent = data)
                        ninchatActivityPresenter.openQueueActivity(this, queueId = ninchatActivityPresenter.ninchatActivityModel.queueId)
                    } else {
                        finish()
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    finish()
                }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onQueueUpdate() {
        ninchatActivityPresenter.setQueueAdapter(
                recyclerView = ninchat_activity_queue_list,
                mActivity = this,
                closeButton = ninchat_activity_close,
                motD = ninchat_activity_motd,
                noQueue = ninchat_activity_no_queues
        )
    }

    override fun onConfigurationFetched() {
        ninchatActivityPresenter.updateActivityView(
                topHeader = ninchat_activity_header,
                closeButton = ninchat_activity_close,
                motD = ninchat_activity_motd,
                noQueue = ninchat_activity_no_queues
        )
    }
}