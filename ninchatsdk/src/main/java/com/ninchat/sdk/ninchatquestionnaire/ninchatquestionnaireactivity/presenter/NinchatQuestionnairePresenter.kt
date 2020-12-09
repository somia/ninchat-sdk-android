package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.presenter

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.model.NinchatQuestionnaireModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view.NinchatQuestionnaireActivity
import kotlinx.android.synthetic.main.activity_ninchat_questionnaire.*
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