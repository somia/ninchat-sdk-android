package com.ninchat.sdk.ninchatqueuelist.presenter

import android.app.Activity
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.presenter.NinchatQuestionnairePresenter
import com.ninchat.sdk.ninchatqueuelist.model.NinchatQueue
import com.ninchat.sdk.ninchatqueue.model.NinchatQueueModel
import com.ninchat.sdk.ninchatqueue.presenter.NinchatQueuePresenter

class NinchatQueueListPresenter(queueList: List<NinchatQueue>) {
    private val queueList = arrayListOf<NinchatQueue>()
            .apply {
                addAll(queueList)
            }

    fun add(queue: NinchatQueue, callback: (queueSize: Int) -> Unit) {
        queueList.add(queue)
        callback(queueList.size)
    }

    fun isClosedQueue(at: Int): Boolean {
        return queueList.getOrNull(at)?.isClosed ?: false
    }

    fun getQueueId(at: Int): String {
        return queueList.getOrNull(at)?.id ?: ""
    }

    fun getQueueName(at: Int): String {
        return NinchatSessionManager.getInstance()?.let { ninchatSessionManager ->
            get(at)?.let { currentQueue ->
                ninchatSessionManager.ninchatState?.siteConfig?.getQueueName(
                        name = currentQueue.name ?: "",
                        closed = currentQueue.isClosed
                )
            }
        } ?: ""
    }

    fun requireOpenQuestionnaireActivity(): Boolean {
        return NinchatSessionManager.getInstance()?.let { ninchatSessionManager ->
            return !ninchatSessionManager.ninchatSessionHolder.isResumedSession() &&
                    ninchatSessionManager.ninchatState?.ninchatQuestionnaire?.hasPreAudienceQuestionnaire() ?: false
        } ?: false
    }

    fun openQueueActivity(activity: Activity?, queueId: String) {
        activity?.startActivityForResult(
                NinchatQueuePresenter.getLaunchIntentWithQueueId(activity, queueId),
                NinchatQueueModel.REQUEST_CODE)
    }

    fun openQuestionnaireActivity(activity: Activity?, queueId: String) {
        activity?.startActivityForResult(
                NinchatQuestionnairePresenter.getLaunchIntent(activity, queueId,
                        NinchatQuestionnaireTypeUtil.PRE_AUDIENCE_QUESTIONNAIRE),
                NinchatQuestionnairePresenter.REQUEST_CODE)
    }

    fun get(at: Int): NinchatQueue? = queueList.getOrNull(at)
    fun size() = queueList.size
}