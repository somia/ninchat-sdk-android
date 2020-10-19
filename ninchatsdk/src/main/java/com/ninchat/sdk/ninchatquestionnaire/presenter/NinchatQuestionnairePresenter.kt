package com.ninchat.sdk.ninchatquestionnaire.presenter

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.events.OnCompleteQuestionnaire
import com.ninchat.sdk.models.questionnaire.conversation.NinchatConversationQuestionnaire
import com.ninchat.sdk.models.questionnaire.form.NinchatFormQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.model.NinchatQuestionnaireModel
import com.ninchat.sdk.ninchatquestionnaire.view.NinchatQuestionnaireActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


interface INinchatQuestionnairePresenter {
    fun onCompleteQuestionnaire(intent: Intent)
}

class NinchatQuestionnairePresenter(
        val ninchatQuestionnaireModel: NinchatQuestionnaireModel,
        val iNinchatQuestionnairePresenter: INinchatQuestionnairePresenter,
) {
    init {
        // register event bus
        EventBus.getDefault().register(this)
    }

    var ninchatConversationQuestionnaire: NinchatConversationQuestionnaire? = null
    var ninchatFormQuestionnaire: NinchatFormQuestionnaire? = null

    fun updateQueueId(intent: Intent?) {
        // update queue id
        intent?.getStringExtra(NinchatQuestionnaireModel.QUEUE_ID)?.let {
            ninchatQuestionnaireModel.queueId = it
        }
    }

    fun updateQuestionnaireType(intent: Intent?) {
        intent?.getIntExtra(NinchatQuestionnaireModel.QUESTIONNAIRE_TYPE, NinchatQuestionnaireModel.POST_AUDIENCE_QUESTIONNAIRE)?.let {
            ninchatQuestionnaireModel.questionnaireType = it
        }
    }

    fun isConversationLikeQuestionnaire(): Boolean {
        return ninchatQuestionnaireModel.isConversationLikeQuestionnaire(questionnaireType = ninchatQuestionnaireModel.questionnaireType)
    }

    fun renderConversationLikeQuestionnaire(recyclerView: RecyclerView, context: Context, layoutManager: LinearLayoutManager) {
        ninchatConversationQuestionnaire = NinchatConversationQuestionnaire(
                ninchatQuestionnaireModel.queueId,
                ninchatQuestionnaireModel.questionnaireType,
                Pair(ninchatQuestionnaireModel.getBotName(), ninchatQuestionnaireModel.getBotAvatar()),
                recyclerView,
                layoutManager
        )
        ninchatConversationQuestionnaire?.setAdapter(context)
    }

    fun renderFormLikeQuestionnaire(recyclerView: RecyclerView, context: Context) {
        ninchatFormQuestionnaire = NinchatFormQuestionnaire(ninchatQuestionnaireModel.queueId,
                ninchatQuestionnaireModel.questionnaireType,
                recyclerView)
        ninchatFormQuestionnaire?.setAdapter(context)
    }

    fun dispose() {
        ninchatConversationQuestionnaire?.dispose()
        ninchatFormQuestionnaire?.dispose()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    @JvmName("onCompleteQuestionnaire")
    fun onCompleteQuestionnaire(onCompleteQuestionnaire: OnCompleteQuestionnaire) {
        val intent = Intent().apply {
            putExtra(NinchatQuestionnaireModel.QUEUE_ID, onCompleteQuestionnaire.queueId)
            putExtra(NinchatQuestionnaireModel.OPEN_QUEUE, onCompleteQuestionnaire.openQueueView)
        }
        iNinchatQuestionnairePresenter.onCompleteQuestionnaire(intent)
    }

    companion object {
        val REQUEST_CODE = NinchatQuestionnairePresenter::class.java.hashCode() and 0xffff
        fun getLaunchIntent(context: Context?, queueId: String?, questionnaireType: Int): Intent {
            return Intent(context, NinchatQuestionnaireActivity::class.java).apply {
                putExtra(NinchatQuestionnaireModel.QUEUE_ID, queueId)
                putExtra(NinchatQuestionnaireModel.QUESTIONNAIRE_TYPE, questionnaireType)
            }
        }
    }
}