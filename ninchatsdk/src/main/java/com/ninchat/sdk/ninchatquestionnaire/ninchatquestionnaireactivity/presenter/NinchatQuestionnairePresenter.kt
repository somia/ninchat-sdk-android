package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.presenter

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.client.Props
import com.ninchat.client.Strings
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.model.NinchatQuestionnaireAnswers
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.model.NinchatQuestionnaireModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view.NinchatQuestionnaireActivity
import kotlinx.coroutines.launch
import org.json.JSONObject

class NinchatQuestionnairePresenter(
        val viewCallback: INinchatQuestionnairePresenter,
) {
    private val model = NinchatQuestionnaireModel()

    fun renderCurrentView(intent: Intent?) {
        model.update(intent)
        viewCallback.renderQuestionnaireList(model.questionnaireList, model.queueId, model.isFormLike)
    }

    fun handleDataSetChange(mRecyclerView: RecyclerView?) {
        mRecyclerView?.let {
            val previousAdapter = it.adapter
            it.adapter = null
            it.adapter = previousAdapter
        }
    }

    fun updateAnswers(answerList: List<JSONObject>) {
        model.answers = NinchatQuestionnaireAnswers().apply {
            parse(questionnaireList = answerList)
        }
        // update queue from answers
        model.answers?.queueId?.let {
            model.queueId = it
        }
    }

    fun isQueueClosed(): Boolean {
        val isClosed = NinchatSessionManager.getInstance()?.ninchatState?.queues?.find { it.id == model.queueId }?.isClosed
        // if null -> queue closed
        // if close -> queue closed
        return isClosed ?: true
    }

    fun hasAnswers(): Boolean {
        return model.answers?.answerList.isNullOrEmpty().not() || model.answers?.tagList.isNullOrEmpty().not()
    }

    fun getAnswersAsProps(): Props {
        val answers = Props()
        if (model.answers?.answerList.isNullOrEmpty().not()) {
            model.answers?.answerList?.forEach {
                answers.setString(it.first, it.second)
            }
        }
        if (model.answers?.tagList.isNullOrEmpty().not()) {
            val tags = Strings()
            model.answers?.tagList?.forEach { tags.append(it) }
            answers.setStringArray("tags", tags)
        }
        return answers
    }

    fun mayBeRegisterAudience() {
        val questionnaireAnswers = getAnswersAsProps()
        val audienceMetadata = model.audienceMetadata().get()?: Props()
        audienceMetadata.apply {
            setObject("pre_answers", questionnaireAnswers)
        }

        // send audience metadata
        NinchatSessionManager.getInstance()?.let {

        }

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

interface INinchatQuestionnairePresenter {
    fun renderQuestionnaireList(questionnaireList: List<JSONObject>, queueId: String?, isFormLike: Boolean)
}