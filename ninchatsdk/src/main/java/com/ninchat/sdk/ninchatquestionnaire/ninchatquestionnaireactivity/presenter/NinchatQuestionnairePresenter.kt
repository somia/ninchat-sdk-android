package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.presenter

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.client.Props
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.helper.glidewrapper.GlideWrapper
import com.ninchat.sdk.models.getTitleBarInfoForQuestionnaire
import com.ninchat.sdk.models.shouldHideTitleBar
import com.ninchat.sdk.networkdispatchers.NinchatDeleteUser
import com.ninchat.sdk.networkdispatchers.NinchatPartChannel
import com.ninchat.sdk.networkdispatchers.NinchatRegisterAudience
import com.ninchat.sdk.networkdispatchers.NinchatSendPostAudienceQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireConstants
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.model.NinchatQuestionnaireAnswers
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.model.NinchatQuestionnaireModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view.NinchatQuestionnaireActivity
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.view.NinchatQuestionnaireListAdapter
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
import kotlinx.android.synthetic.main.activity_ninchat_queue.view.*
import kotlinx.android.synthetic.main.bot_item_conversation_view.view.*
import kotlinx.android.synthetic.main.ninchat_titlebar.view.*
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
            preAnswers = if (model.questionnaireType == NinchatQuestionnaireConstants.preAudienceQuestionnaire) model.preAnswers() else listOf(),
            queueId = model.queueId,
            isFormLike = model.isFormLike
        )
    }

    fun handleDataSetChange(
        mRecyclerView: RecyclerView?,
        myAdapter: NinchatQuestionnaireListAdapter,
        withError: Boolean
    ) {
        if (withError) {
            // if it was triggered by error then only call notify dataset change since list is still same
            myAdapter.notifyDataSetChanged()
            return
        }
        mRecyclerView?.apply {
            adapter = null
            this.adapter = myAdapter
        }
    }

    fun showThankYouText(adapter: NinchatQuestionnaireListAdapter, isComplete: Boolean) {
        adapter.showThankYou(isComplete)
    }

    fun showNextQuestionnaire(
        adapter: NinchatQuestionnaireListAdapter,
        onNextQuestionnaire: OnNextQuestionnaire
    ) {
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
            NinchatScopeHandler.getIOScope()
                .launch(CoroutineExceptionHandler(handler = { _, _ -> viewCallback.onAudienceRegisterError() })) {
                    val id = NinchatRegisterAudience.execute(
                        currentSession = it,
                        queueId = model.queueId,
                        audienceMetadata = audienceMetadata
                    )
                    if (id == -1L) {
                        viewCallback.onAudienceRegisterError()
                    } else {
                        NinchatSessionManager.getInstance()?.ninchatState?.actionId = id
                    }
                }
        }
    }

    fun mayBeSendPostAudienceQuestionnaire() {
        val questionnaireAnswers = model.getAnswersAsJson()
        val payload = JSONObject().apply {
            putOpt("data", JSONObject().apply {
                putOpt("post_answers", questionnaireAnswers)
            })
            putOpt("time", System.currentTimeMillis())
        }
        NinchatSessionManager.getInstance()?.session?.let {
            // even if the error occurred,
            NinchatScopeHandler.getIOScope()
                .launch(CoroutineExceptionHandler(handler = { _, _ -> viewCallback.onCompletePostAudienceQuestionnaire() })) {
                    val id = NinchatSendPostAudienceQuestionnaire.execute(
                        currentSession = it,
                        channelId = NinchatSessionManager.getInstance().ninchatState?.channelId,
                        message = payload.toString(2)
                    )
                    if (id == -1L) {
                        viewCallback.onCompletePostAudienceQuestionnaire()
                    } else {
                        NinchatSessionManager.getInstance()?.ninchatState?.actionId = id
                    }
                }
        }
    }

    fun handlePostAudienceQuestionnaire(callback: () -> Unit) {
        NinchatSessionManager.getInstance()?.session?.let {
            NinchatScopeHandler.getIOScope()
                .launch(CoroutineExceptionHandler(handler = { _, _ -> callback() })) {
                    NinchatPartChannel.execute(
                        currentSession = it,
                        channelId = NinchatSessionManager.getInstance().ninchatState?.channelId
                    )

                    NinchatDeleteUser.execute(currentSession = it)
                    Handler().postDelayed({
                        it.close()
                        callback()
                    }, 500)
                }
        }
    }

    fun mayBeAttachTitlebar(view: View, callback: () -> Unit) {
        val titleBarInfo = getTitleBarInfoForQuestionnaire()
        if (shouldHideTitleBar() || titleBarInfo == null) return
        when {
            // no questionnaire avatar and no questionnaire name
            !titleBarInfo.showAvatar || titleBarInfo.name.isNullOrEmpty() -> {
                view.ninchat_titlebar.ninchat_chat_titlebar_avatar_text_placeholder.visibility =
                    View.VISIBLE
                view.ninchat_titlebar.ninchat_chat_titlebar_avatar_img.visibility = View.VISIBLE
                view.ninchat_titlebar.ninchat_chat_titlebar_avatar_img.setImageResource(R.drawable.ninchat_chat_avatar_placeholder)
            }
            // questionnaire avatar and questionnaire name
            titleBarInfo.showAvatar && !titleBarInfo.name.isNullOrEmpty() -> {
                view.ninchat_titlebar.ninchat_titlebar_agent_info_parent.visibility = View.VISIBLE
                view.ninchat_titlebar.ninchat_chat_titlebar_avatar_img.visibility = View.VISIBLE

                view.ninchat_titlebar.ninchat_chat_titlebar_agent_name.text = titleBarInfo.name
                view.ninchat_titlebar.ninchat_chat_titlebar_agent_jobtitle.visibility = View.GONE
                try {
                    GlideWrapper.loadImageAsCircle(
                        view.context,
                        titleBarInfo.userAvatar,
                        view.ninchat_titlebar.ninchat_chat_titlebar_avatar_img
                    )
                } catch (e: Exception) {
                    view.ninchat_titlebar.ninchat_chat_titlebar_avatar_img.setImageResource(R.drawable.ninchat_chat_avatar_left)
                }
            }
            // no questionnaire avatar and only questionnaire name
            !titleBarInfo.showAvatar && !titleBarInfo.name.isNullOrEmpty() -> {
                view.ninchat_titlebar.ninchat_titlebar_agent_info_parent.visibility = View.VISIBLE

                view.ninchat_titlebar.ninchat_chat_titlebar_agent_name.text = titleBarInfo.name
                view.ninchat_titlebar.ninchat_chat_titlebar_agent_jobtitle.visibility = View.GONE
            }
        }
        view.ninchat_chat_close.text = titleBarInfo.closeButtonText
        view.visibility = View.VISIBLE
        view.ninchat_chat_close.setOnClickListener {
            callback()
        }
    }

    fun isComplete(): Boolean = model.fromComplete
    fun queueId(): String? = model.queueId
    fun isPostAudienceQuestionnaire(): Boolean =
        model.questionnaireType == NinchatQuestionnaireConstants.postAudienceQuestionnaire

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
    fun renderQuestionnaireList(
        questionnaireList: List<JSONObject>,
        preAnswers: List<Pair<String, Any>>,
        queueId: String?,
        isFormLike: Boolean
    )

    fun onCompleteQuestionnaire()
    fun onAudienceRegisterError()
    fun onCompletePostAudienceQuestionnaire()
}