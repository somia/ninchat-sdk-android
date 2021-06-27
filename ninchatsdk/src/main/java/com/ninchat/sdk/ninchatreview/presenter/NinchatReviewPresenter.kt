package com.ninchat.sdk.ninchatreview.presenter

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.helper.glidewrapper.GlideWrapper
import com.ninchat.sdk.networkdispatchers.NinchatSendRatings
import com.ninchat.sdk.ninchatreview.model.NinchatReviewModel
import com.ninchat.sdk.ninchatreview.view.NinchatReviewActivity
import com.ninchat.sdk.ninchattitlebar.model.shouldShowTitlebar
import com.ninchat.sdk.ninchattitlebar.view.NinchatTitlebarView
import com.ninchat.sdk.utils.misc.Misc
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
import kotlinx.android.synthetic.main.activity_ninchat_review.view.*
import kotlinx.android.synthetic.main.bot_writing_indicator.view.*
import kotlinx.android.synthetic.main.review_rating_bot_view.view.*
import kotlinx.android.synthetic.main.review_rating_bot_view.view.ninchat_chat_message_bot_avatar
import kotlinx.android.synthetic.main.review_rating_bot_view.view.ninchat_chat_message_bot_text
import kotlinx.android.synthetic.main.review_rating_icon_items.view.*
import kotlinx.android.synthetic.main.review_rating_text_view.view.*
import kotlinx.coroutines.launch
import org.json.JSONException


class NinchatReviewPresenter(
    val ninchatReviewModel: NinchatReviewModel,
) {

    fun getResultIntent(): Intent {
        val rating = ninchatReviewModel.currentRating
        return Intent().putExtra(NinchatSession.Analytics.Keys.RATING, rating)
    }

    fun renderFormView(formView: View, botView: View, rootActivity: View) {
        setVisibility(view = formView, visible = !isConversationView())
        setVisibility(view = botView, visible = isConversationView())
        renderText(view = formView, rootActivity = rootActivity)

        // initially review description was not visible. so make it visible on load
        formView.ninchat_review_description.visibility = View.VISIBLE
    }

    fun renderBotView(formView: View, botView: View, rootActivity: View) {
        setVisibility(view = formView, visible = !isConversationView())
        setVisibility(view = botView, visible = isConversationView())

        renderBotViewInternal(botView, rootActivity, callback = {
            botView.ninchat_chat_message_bot_writing_review_root.visibility = View.GONE
            botView.ninchat_bot_rating_text_root_view.visibility = View.VISIBLE
            botView.ninchat_bot_ratings_icon_items_root_view.visibility = View.VISIBLE

            botView.ninchat_bot_rating_text_root_view.background = ContextCompat.getDrawable(
                botView.context,
                R.drawable.ninchat_chat_questionnaire_background
            )

            // render text now
            renderText(view = botView, rootActivity = rootActivity)

            // for bot like view, they should place in the beginning
            botView.ninchat_review_title.gravity = Gravity.START
            botView.ninchat_review_description.gravity = Gravity.START
        })
    }

    private fun renderBotViewInternal(view: View, rootActivity: View, callback: () -> Unit) {
        //1: set rating text and icon invisible
        view.ninchat_bot_rating_text_root_view.visibility = View.GONE
        view.ninchat_bot_ratings_icon_items_root_view.visibility = View.GONE

        //2: set background
        val drawableBackground = NinchatSessionManager.getInstance()?.ninchatChatBackground?.let {
            Misc.getNinchatChatBackground(view.context, it)
        } ?: ContextCompat.getDrawable(view.context, R.drawable.ninchat_chat_background_tiled)
        rootActivity.background = drawableBackground
        view.ninchat_chat_message_bot_writing_review_root.background = ContextCompat.getDrawable(
            view.context,
            R.drawable.ninchat_chat_questionnaire_background
        )

        //3: set bot details
        view.ninchat_chat_message_bot_text.text = ninchatReviewModel.getBotName()
        if (!shouldShowTitlebar() && !hideAvatar()) {
            ninchatReviewModel.getBotAvatar()?.let {
                GlideWrapper.loadImageAsCircle(
                    view.context,
                    it,
                    view.ninchat_chat_message_bot_avatar,
                    R.drawable.ninchat_chat_avatar_left
                )
            }
            view.ninchat_chat_message_bot_avatar.visibility = View.VISIBLE
        }

        //4: start dummy animation
        view.ninchat_chat_message_bot_writing.setBackgroundResource(R.drawable.ninchat_icon_chat_writing_indicator)
        val animationDrawable =
            (view.ninchat_chat_message_bot_writing.background) as AnimationDrawable
        animationDrawable.start()

        //5: stop animation after 1.5 second
        Handler().postDelayed({
            animationDrawable.stop()
            callback()
        }, 1500)

    }

    private fun renderText(view: View, rootActivity: View) {
        view.ninchat_review_title.text =
            Misc.toRichText(ninchatReviewModel.getFeedbackTitleText(), view.ninchat_review_title)
        view.ninchat_review_positive.text = ninchatReviewModel.getFeedbackPositiveText()
        view.ninchat_review_neutral.text = ninchatReviewModel.getFeedbackNeutralText()
        view.ninchat_review_negative.text = ninchatReviewModel.getFeedbackNegativeText()
        rootActivity.ninchat_review_skip.text = ninchatReviewModel.getFeedbackSkipText()
    }

    private fun setVisibility(view: View, visible: Boolean = false) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun skippedReview() {
        NinchatSessionManager.getInstance()?.ninchatState?.skippedReview = true
    }

    fun maybeSendRating(rating: Int) {
        // set ratings
        ninchatReviewModel.currentRating = rating
        if (rating == NinchatSession.Analytics.Rating.NO_ANSWER) skippedReview()

        // check if it is a no answer or rating
        if (rating != NinchatSession.Analytics.Rating.NO_ANSWER) {
            try {
                val messagePayload = ninchatReviewModel.getRatingPayload()
                NinchatSessionManager.getInstance()?.let { ninchatSessionManager ->
                    NinchatScopeHandler.getIOScope().launch {
                        NinchatSendRatings.execute(
                            currentSession = ninchatSessionManager.session,
                            channelId = ninchatSessionManager.ninchatState?.channelId,
                            message = messagePayload.toString(2)
                        )
                    }
                }
            } catch (e: JSONException) {
                // Ignore
            }
        }
    }

    fun isConversationView(): Boolean {
        return ninchatReviewModel.isConversationLikeQuestionnaire()
    }

    fun mayBeAttachTitlebar(view: View, callback: () -> Unit) {
        NinchatTitlebarView.showTitlebarForReview(view = view, callback = callback)
    }

    fun hideAvatar() =
        NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.hideAgentAvatar() ?: false

    companion object {
        @JvmStatic
        fun getLaunchIntent(context: Context?): Intent {
            return Intent(context, NinchatReviewActivity::class.java)
        }
    }

}