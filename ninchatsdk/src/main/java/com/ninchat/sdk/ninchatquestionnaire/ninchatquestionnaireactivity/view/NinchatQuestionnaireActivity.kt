package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.activities.NinchatBaseActivity
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.presenter.INinchatQuestionnairePresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.presenter.NinchatQuestionnairePresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.view.NinchatQuestionnaireListAdapter
import kotlinx.android.synthetic.main.activity_ninchat_questionnaire.*
import org.json.JSONObject

class NinchatQuestionnaireActivity : NinchatBaseActivity(), INinchatQuestionnairePresenter, QuestionnaireActivityCallback {
    private val presenter = NinchatQuestionnairePresenter(viewCallback = this)

    override val layoutRes: Int
        get() = R.layout.activity_ninchat_questionnaire

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.renderCurrentView(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
    }

    fun onClose(view: View?) {
        setResult(RESULT_CANCELED, null)
        finish()
    }

    override fun renderQuestionnaireList(questionnaireList: List<JSONObject>, queueId: String?, isFormLike: Boolean) {
        val mRecyclerView = questionnaire_form_rview as RecyclerView
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.adapter = NinchatQuestionnaireListAdapter(
                questionnaireList = questionnaireList,
                isFormLike = isFormLike,
                rootActivityCallback = this
        )
    }

    override fun onRegistered(answerList: List<JSONObject>) {
        presenter.updateAnswers(answerList = answerList)
        presenter.mayBeRegisterAudience()
    }

    override fun onComplete(answerList: List<JSONObject>) {
        presenter.updateAnswers(answerList = answerList)
    }

    override fun onDataSetChange() {
        presenter.handleDataSetChange(questionnaire_form_rview as RecyclerView)
    }

    override fun onFinish() {

    }
}

interface QuestionnaireActivityCallback {
    fun onRegistered(answerList: List<JSONObject>)
    fun onComplete(answerList: List<JSONObject>)
    fun onDataSetChange()
    fun onFinish()
}