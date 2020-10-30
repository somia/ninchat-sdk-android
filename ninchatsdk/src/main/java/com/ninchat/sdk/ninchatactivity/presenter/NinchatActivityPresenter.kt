package com.ninchat.sdk.ninchatactivity.presenter

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil
import com.ninchat.sdk.ninchatactivity.model.NinchatActivityModel
import com.ninchat.sdk.ninchatactivity.view.NinchatActivity
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.presenter.NinchatQuestionnairePresenter
import com.ninchat.sdk.ninchatqueue.model.NinchatQueueModel
import com.ninchat.sdk.ninchatqueue.presenter.NinchatQueuePresenter
import com.ninchat.sdk.utils.misc.Misc.Companion.toRichText


interface INinchatActivityPresenter {
    fun onQueueUpdate()
    fun onConfigurationFetched()
}

class NinchatActivityPresenter(
        val ninchatActivityModel: NinchatActivityModel,
        val iNinchatActivityPresenter: INinchatActivityPresenter,
        val mContext: Context,
) {

    fun updateQueueId(intent: Intent?) {
        // update queue id
        intent?.getStringExtra(NinchatActivityModel.QUEUE_ID)?.let {
            ninchatActivityModel.queueId = it
        }
    }

    fun hasQueue(): Boolean {
        return !ninchatActivityModel.queueId.isNullOrEmpty()
    }

    fun hasSession(): Boolean {
        return NinchatSessionManager.getInstance() != null
    }


    fun shouldOpenPreAudienceQuestionnaireActivity(): Boolean {
        return NinchatSessionManager.getInstance()?.let { ninchatSessionManager ->
            return !ninchatSessionManager.ninchatSessionHolder.isResumedSession() &&
                    ninchatSessionManager.ninchatState?.ninchatQuestionnaire?.hasPreAudienceQuestionnaire() ?: false
        } ?: false
    }

    fun shouldOpenPostAudienceQuestionnaireActivity(): Boolean {
        return NinchatSessionManager.getInstance()?.let { ninchatSessionManager ->
            return ninchatSessionManager.ninchatState?.ninchatQuestionnaire?.hasPostAudienceQuestionnaire() ?: false
        } ?: false
    }

    fun updateActivityView(
            topHeader: TextView,
            closeButton: Button,
            motD: TextView,
            noQueue: TextView,
    ) {
        topHeader.text = toRichText(ninchatActivityModel.getWelcomeMessage(), topHeader)
        closeButton.text = ninchatActivityModel.getCloseWindowText()
        closeButton.visibility = View.VISIBLE
        motD.text = toRichText(ninchatActivityModel.getMotD(), motD)
        noQueue.text = toRichText(ninchatActivityModel.getNoQueueText(), noQueue)
    }

    fun setQueueAdapter(
            recyclerView: RecyclerView,
            mActivity: NinchatActivity,
            closeButton: Button,
            motD: TextView,
            noQueue: TextView,
    ) {
        val ninchatQueueListAdapter = NinchatSessionManager.getInstance()?.getNinchatQueueListAdapter(mActivity)
        recyclerView.adapter = ninchatQueueListAdapter
        if (ninchatQueueListAdapter?.itemCount == 0) {
            noQueue.visibility = View.VISIBLE
            motD.text = toRichText(ninchatActivityModel.getMotD(), motD)
            closeButton.visibility = View.VISIBLE
        }
        ninchatQueueListAdapter?.notifyDataSetChanged()
    }

    fun subscribeBroadcaster() {
        LocalBroadcastManager.getInstance(mContext).run {
            registerReceiver(queuesUpdatedReceiver, IntentFilter(NinchatSession.Broadcast.QUEUES_UPDATED));
            registerReceiver(configurationFetchedReceiver, IntentFilter(NinchatSession.Broadcast.CONFIGURATION_FETCHED));
        }
    }

    fun unSubscribeBroadcaster() {
        LocalBroadcastManager.getInstance(mContext).run {
            unregisterReceiver(queuesUpdatedReceiver)
            unregisterReceiver(configurationFetchedReceiver)
        }
    }


    var queuesUpdatedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (NinchatSession.Broadcast.QUEUES_UPDATED == action) {
                // setQueueAdapter()
                iNinchatActivityPresenter.onQueueUpdate()
            }
        }
    }

    var configurationFetchedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (NinchatSession.Broadcast.CONFIGURATION_FETCHED == intent.action) {
                // setTexts()
                iNinchatActivityPresenter.onConfigurationFetched()
            }
        }
    }

    fun openPostAudienceQuestionnaireActivity(activity: Activity?, queueId: String?) {
        activity?.startActivityForResult(
                NinchatQuestionnairePresenter.getLaunchIntent(activity, queueId,
                        NinchatQuestionnaireTypeUtil.POST_AUDIENCE_QUESTIONNAIRE),
                NinchatQuestionnairePresenter.REQUEST_CODE)
    }

    fun openPreAudienceQuestionnaireActivity(activity: Activity?, queueId: String?) {
        activity?.startActivityForResult(
                NinchatQuestionnairePresenter.getLaunchIntent(activity, queueId,
                        NinchatQuestionnaireTypeUtil.PRE_AUDIENCE_QUESTIONNAIRE),
                NinchatQuestionnairePresenter.REQUEST_CODE)
    }

    fun openQueueActivity(mActivity: NinchatActivity?, queueId: String?) {
        mActivity?.startActivityForResult(NinchatQueuePresenter.getLaunchIntentWithQueueId(mActivity, queueId), NinchatQueueModel.REQUEST_CODE)
    }

    companion object {
        fun getLaunchIntent(context: Context?, queueId: String?): Intent? {
            return Intent(context, NinchatActivity::class.java)
                    .putExtra(NinchatActivityModel.QUEUE_ID, queueId)
        }
    }
}