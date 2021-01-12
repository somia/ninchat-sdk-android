package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.activities.NinchatBaseActivity
import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.events.OnPostAudienceQuestionnaire
import com.ninchat.sdk.events.OnSubmitQuestionnaireAnswers
import com.ninchat.sdk.helper.NinchatQuestionnaireItemDecoration
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
    private lateinit var currentAdapter: NinchatQuestionnaireListAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    override val layoutRes: Int
        get() = R.layout.activity_ninchat_questionnaire

    override fun onCreate(savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
        super.onCreate(savedInstanceState)
        mLayoutManager = LinearLayoutManager(applicationContext)
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

    override fun renderQuestionnaireList(questionnaireList: List<JSONObject>, preAnswers: List<Pair<String, Any>>, queueId: String?, isFormLike: Boolean) {
        currentAdapter = NinchatQuestionnaireListAdapter(
                questionnaireList = questionnaireList,
                preAnswers = preAnswers,
                isFormLike = isFormLike,
                rootActivityCallback = this
        ).apply {
            setHasStableIds(true)
        }
        val spaceInPixelTop = applicationContext.resources.getDimensionPixelSize(R.dimen.ninchat_questionnaire_items_margin_top)
        val spaceLeft = applicationContext.resources.getDimensionPixelSize(R.dimen.ninchat_questionnaire_items_margin_left)
        val spaceRight = applicationContext.resources.getDimensionPixelSize(R.dimen.ninchat_questionnaire_items_margin_right)
        (questionnaire_form_rview as RecyclerView).apply {
            this.layoutManager = mLayoutManager
            this.adapter = currentAdapter
            addItemDecoration(NinchatQuestionnaireItemDecoration(spaceInPixelTop, spaceLeft, spaceRight))
        }
    }

    override fun onCompleteQuestionnaire() {
        onFinishQuestionnaire(openQueue = true)
    }

    override fun onAudienceRegisterError() {
        onFinishQuestionnaire(openQueue = false)
    }

    override fun onCompletePostAudienceQuestionnaire() {
        presenter.handlePostAudienceQuestionnaire {
            onFinishQuestionnaire(openQueue = false)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @JvmName("OnSubmitQuestionnaireAnswers")
    fun onSubmitQuestionnaireAnswers(result: OnSubmitQuestionnaireAnswers) {
        if (result.withError) {
            onAudienceRegisterError()
            return
        }
        // else show thank you text
        presenter.showThankYouText(currentAdapter, isComplete = presenter.isComplete())
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @JvmName("OnPostAudienceQuestionnaire")
    fun onSubmitPostQuestionnaireAnswers(result: OnPostAudienceQuestionnaire) {
        // else show thank you text
        onCompletePostAudienceQuestionnaire()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @JvmName("onNextQuestionnaire")
    fun onNextQuestionnaire(onNextQuestionnaire: OnNextQuestionnaire) {
        currentAdapter.showNextQuestionnaire(onNextQuestionnaire)
    }

    override fun onRegistered(answerList: List<JSONObject>) {
        presenter.updateAnswers(answerList = answerList)
        presenter.mayBeRegisterAudience()
    }

    override fun onComplete(answerList: List<JSONObject>) {
        presenter.updateAnswers(answerList = answerList)
        if (presenter.isPostAudienceQuestionnaire())
            presenter.mayBeSendPostAudienceQuestionnaire()
        else
            presenter.mayeBeCompleteQuestionnaire()
    }

    override fun onDataSetChange(withError: Boolean) {
        presenter.handleDataSetChange(questionnaire_form_rview as RecyclerView, myAdapter = currentAdapter, withError = withError)
    }

    override fun onFinishQuestionnaire(openQueue: Boolean) {
        val intent = Intent().apply {
            putExtra(NinchatQuestionnaireModel.QUEUE_ID, presenter.queueId())
            putExtra(NinchatQuestionnaireModel.OPEN_QUEUE, openQueue)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun scrollTo(position: Int) {
        val heightOffset = applicationContext.resources.getDimensionPixelSize(R.dimen.ninchat_questionnaire_item_bot_height)
        mLayoutManager.scrollToPositionWithOffset(position, heightOffset)
    }
}

interface QuestionnaireActivityCallback {
    fun onRegistered(answerList: List<JSONObject>)
    fun onComplete(answerList: List<JSONObject>)
    fun onDataSetChange(withError: Boolean)
    fun onFinishQuestionnaire(openQueue: Boolean)
    fun scrollTo(position: Int)
}