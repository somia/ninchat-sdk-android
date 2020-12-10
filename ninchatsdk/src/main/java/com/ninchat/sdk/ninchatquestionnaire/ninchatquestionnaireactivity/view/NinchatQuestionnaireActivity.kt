package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.activities.NinchatBaseActivity
import com.ninchat.sdk.events.OnSubmitQuestionnaireAnswers
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.model.NinchatQuestionnaireModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.presenter.INinchatQuestionnairePresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.presenter.NinchatQuestionnairePresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.view.NinchatQuestionnaireListAdapter
import kotlinx.android.synthetic.main.activity_ninchat_questionnaire.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

class NinchatQuestionnaireActivity : NinchatBaseActivity(), INinchatQuestionnairePresenter, QuestionnaireActivityCallback {
    private val presenter = NinchatQuestionnairePresenter(viewCallback = this)

    override val layoutRes: Int
        get() = R.layout.activity_ninchat_questionnaire

    override fun onCreate(savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
        super.onCreate(savedInstanceState)
        presenter.renderCurrentView(intent)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
    }

    fun onClose(view: View?) {
        setResult(RESULT_CANCELED, null)
        super.finish()
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

    override fun onCompleteQuestionnaire() {
        onFinishQuestionnaire(openQueue = true)
    }

    override fun onAudienceRegisterError() {
        onFinishQuestionnaire(openQueue = false)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @JvmName("OnSubmitQuestionnaireAnswers")
    fun onSubmitQuestionnaireAnswers(result: OnSubmitQuestionnaireAnswers) {
        if (result.withError) {
            onAudienceRegisterError()
            return
        }
        // else show thank you text
        presenter.showThankYouText(questionnaire_form_rview as RecyclerView, isComplete = presenter.isComplete())
    }

    override fun onRegistered(answerList: List<JSONObject>) {
        presenter.updateAnswers(answerList = answerList)
        presenter.mayBeRegisterAudience()
    }

    override fun onComplete(answerList: List<JSONObject>) {
        presenter.updateAnswers(answerList = answerList)
        presenter.mayeBeCompleteQuestionnaire()
    }

    override fun onDataSetChange() {
        presenter.handleDataSetChange(questionnaire_form_rview as RecyclerView)
    }

    override fun onFinishQuestionnaire(openQueue: Boolean) {
        val intent = Intent().apply {
            putExtra(NinchatQuestionnaireModel.QUEUE_ID, presenter.queueId())
            putExtra(NinchatQuestionnaireModel.OPEN_QUEUE, openQueue)
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}

interface QuestionnaireActivityCallback {
    fun onRegistered(answerList: List<JSONObject>)
    fun onComplete(answerList: List<JSONObject>)
    fun onDataSetChange()
    fun onFinishQuestionnaire(openQueue: Boolean)
}