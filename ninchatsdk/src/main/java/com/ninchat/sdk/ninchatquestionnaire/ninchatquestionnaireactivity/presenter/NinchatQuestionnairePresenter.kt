package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.presenter

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.client.Props
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.networkdispatchers.NinchatRegisterAudience
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.model.NinchatQuestionnaireAnswers
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.model.NinchatQuestionnaireModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view.NinchatQuestionnaireActivity
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.view.NinchatQuestionnaireListAdapter
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.json.JSONObject

class NinchatQuestionnairePresenter(
        val viewCallback: INinchatQuestionnairePresenter,
) {
    private val model = NinchatQuestionnaireModel()

    fun renderCurrentView(intent: Intent?) {
        model.update(intent)
        viewCallback.renderQuestionnaireList(
                questionnaireList = model.questionnaireList,
                preAnswers = model.preAnswers(),
                queueId = model.queueId,
                isFormLike = model.isFormLike)
    }

    fun handleDataSetChange(mRecyclerView: RecyclerView?, myAdapter: NinchatQuestionnaireListAdapter) {
        mRecyclerView?.apply {
            adapter = null
            this.adapter = myAdapter
        }
    }

    fun showThankYouText(adapter: NinchatQuestionnaireListAdapter, isComplete: Boolean) {
        adapter.showThankYou(isComplete)
    }

    fun showNextQuestionnaire(adapter: NinchatQuestionnaireListAdapter, onNextQuestionnaire: OnNextQuestionnaire) {
        adapter.showNextQuestionnaire(onNextQuestionnaire)
    }

    fun updateAnswers(answerList: List<JSONObject>) {
        model.answers = NinchatQuestionnaireAnswers().apply {
            parse(answerList = answerList)
        }
        // update queue from answers
        if (model.answers?.queueId.isNullOrEmpty().not()) {
            model.queueId = model.answers?.queueId
        }
    }

    fun mayeBeCompleteQuestionnaire() {
        if (model.isQueueClosed()) {
            mayBeRegisterAudience(fromComplete = true)
            return
        }
        val questionnaireAnswers = model.getAnswersAsProps()
        val audienceMetadata = model.audienceMetadata().get() ?: Props()
        audienceMetadata.apply {
            setObject("pre_answers", questionnaireAnswers)
        }
        // update audience metadata
        model.audienceMetadata().set(audienceMetadata)
        viewCallback.onCompleteQuestionnaire()
    }

    fun mayBeRegisterAudience(fromComplete: Boolean = false) {
        model.fromComplete = fromComplete
        val questionnaireAnswers = model.getAnswersAsProps()
        val audienceMetadata = model.audienceMetadata().get() ?: Props()
        audienceMetadata.apply {
            setObject("pre_answers", questionnaireAnswers)
        }
        // send audience metadata
        NinchatSessionManager.getInstance()?.session?.let {
            // even if the error occurred,
            NinchatScopeHandler.getIOScope().launch(CoroutineExceptionHandler(handler = { _, _ -> viewCallback.onAudienceRegisterError() })) {
                val id = NinchatRegisterAudience.execute(
                        currentSession = it,
                        queueId = model.queueId,
                        audienceMetadata = audienceMetadata
                )
                if (id == -1L) {
                    viewCallback.onAudienceRegisterError()
                } else {
                    NinchatSessionManager.getInstance()?.ninchatState?.actionId
                }
            }
        }
    }

    fun isComplete(): Boolean = model.fromComplete
    fun queueId(): String? = model.queueId

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

interface INinchatQuestionnairePresenter {
    fun renderQuestionnaireList(questionnaireList: List<JSONObject>, preAnswers: List<Pair<String, Any>>, queueId: String?, isFormLike: Boolean)
    fun onCompleteQuestionnaire()
    fun onAudienceRegisterError()
}