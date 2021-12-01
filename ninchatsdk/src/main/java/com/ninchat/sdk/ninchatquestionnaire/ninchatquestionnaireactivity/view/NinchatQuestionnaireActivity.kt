package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.activities.NinchatBaseActivity
import com.ninchat.sdk.events.OnItemFocus
import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.events.OnPostAudienceQuestionnaire
import com.ninchat.sdk.events.OnSubmitQuestionnaireAnswers
import com.ninchat.sdk.helper.NinchatQuestionnaireItemDecoration
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.model.NinchatQuestionnaireModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.presenter.INinchatQuestionnairePresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.presenter.NinchatQuestionnairePresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.view.NinchatQuestionnaireListAdapter
import com.ninchat.sdk.utils.keyboard.hideKeyBoardForce
import com.ninchat.sdk.utils.misc.Misc
import kotlinx.android.synthetic.main.activity_ninchat_questionnaire.*
import kotlinx.android.synthetic.main.activity_ninchat_questionnaire.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

class NinchatQuestionnaireActivity : NinchatBaseActivity(), INinchatQuestionnairePresenter,
    QuestionnaireActivityCallback {
    private val presenter = NinchatQuestionnairePresenter(viewCallback = this)
    private lateinit var currentAdapter: NinchatQuestionnaireListAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mRecylerView: RecyclerView

    override val layoutRes: Int
        get() = R.layout.activity_ninchat_questionnaire

    override fun onCreate(savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
        super.onCreate(savedInstanceState)
        val drawableBackground = NinchatSessionManager.getInstance()?.ninchatChatBackground?.let {
            Misc.getNinchatChatBackground(applicationContext, it)
        } ?: ContextCompat.getDrawable(applicationContext, R.drawable.ninchat_chat_background_tiled)
        ninchat_audience_questionnaire_root.background = drawableBackground
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

    override fun renderQuestionnaireList(
        questionnaireList: List<JSONObject>,
        preAnswers: List<Pair<String, Any>>,
        queueId: String?,
        isFormLike: Boolean
    ) {
        currentAdapter = NinchatQuestionnaireListAdapter(
            questionnaireList = questionnaireList,
            preAnswers = preAnswers,
            isFormLike = isFormLike,
            rootActivityCallback = this
        ).apply {
            setHasStableIds(true)
        }
        val spaceInPixelTop =
            applicationContext.resources.getDimensionPixelSize(R.dimen.ninchat_questionnaire_items_margin_top)
        val spaceLeft =
            applicationContext.resources.getDimensionPixelSize(R.dimen.ninchat_questionnaire_items_margin_left)
        val spaceRight =
            applicationContext.resources.getDimensionPixelSize(R.dimen.ninchat_questionnaire_items_margin_right)
        mRecylerView = (questionnaire_form_rview as RecyclerView).apply {
            this.layoutManager = mLayoutManager
            this.adapter = currentAdapter
            addItemDecoration(
                NinchatQuestionnaireItemDecoration(
                    spaceInPixelTop,
                    spaceLeft,
                    spaceRight
                )
            )
        }
        presenter.mayBeAttachTitlebar(
            ninchat_audience_questionnaire_root.ninchat_titlebar,
            callback = {
                onFinishQuestionnaire(openQueue = false)
            })
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
        hideKeyBoardForce()
        currentAdapter.showNextQuestionnaire(onNextQuestionnaire)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @JvmName("onItemFocus")
    fun onItemFocus(onItemFocus: OnItemFocus) {
        if (onItemFocus.actionDone) {
            // hide keyboard
            hideKeyBoardForce()
            return
        }
        if (currentAdapter.isLastElement(onItemFocus.position)) {
            // scroll to position
            mRecylerView.postDelayed({
                mRecylerView.smoothScrollToPosition(currentAdapter.itemCount)
            }, 150)
        }
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
        presenter.handleDataSetChange(
            questionnaire_form_rview as RecyclerView,
            myAdapter = currentAdapter,
            withError = withError
        )
    }

    override fun onFinishQuestionnaire(openQueue: Boolean) {
        val intent = Intent().apply {
            putExtra(NinchatQuestionnaireModel.QUEUE_ID, presenter.queueId())
            putExtra(NinchatQuestionnaireModel.OPEN_QUEUE, openQueue)
            putExtra(NinchatQuestionnaireModel.OPEN_QUEUE, openQueue)
        }
        presenter.savePreAudienceQuestionnaireMessage()
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun scrollTo(position: Int) {
        val heightOffset =
            applicationContext.resources.getDimensionPixelSize(R.dimen.ninchat_questionnaire_item_bot_height)
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