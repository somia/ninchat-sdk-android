package com.ninchat.sdk.ninchatquestionnaire.ninchatconversationquestionnaire.view

import android.graphics.drawable.AnimationDrawable
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.GlideApp
import com.ninchat.sdk.R
import com.ninchat.sdk.events.OnComponentError
import com.ninchat.sdk.events.OnItemLoaded
import com.ninchat.sdk.helper.NinchatQuestionnaireItemDecoration
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemSetter
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.lang.ref.WeakReference

class NinchatConversationViewHolder(
        itemView: View,
        questionnaireElement: JSONObject?,
        botDetails: Pair<String?, String?>?,
        position: Int
) : RecyclerView.ViewHolder(itemView) {
    private val TAG = NinchatConversationViewHolder::class.java.simpleName
    var mTextView: TextView
    var mImageView: ImageView
    var mBotImageView: ImageView
    private val mRecyclerView: RecyclerView
    private var mFormLikeAudienceQuestionnaireAdapter: NinchatFormQuestionnaireAdapter? = null
    private val mQuestionnaireElementWeakReference: WeakReference<JSONObject?>
    fun bind(questionnaireElement: JSONObject?, botDetails: Pair<String?, String?>?, position: Int) {
        mTextView.text = NinchatQuestionnaireItemGetter.getBotName(botDetails)
        if (!TextUtils.isEmpty(NinchatQuestionnaireItemGetter.getBotAvatar(botDetails))) {
            // has bot image utl
            try {
                GlideApp.with(itemView.context)
                        .load(NinchatQuestionnaireItemGetter.getBotAvatar(botDetails))
                        .circleCrop()
                        .into(mBotImageView)
            } catch (e: Exception) {
                mImageView.setImageResource(R.drawable.ninchat_chat_avatar_left)
            }
        }
        mImageView.setBackgroundResource(R.drawable.ninchat_icon_chat_writing_indicator)
        val animationDrawable = mImageView.background as AnimationDrawable
        animationDrawable.start()
        mRecyclerView.postDelayed({
            animationDrawable.stop()
            itemView.findViewById<View>(R.id.ninchat_chat_message_bot_writing_root).visibility = View.GONE
            mFormLikeAudienceQuestionnaireAdapter = NinchatFormQuestionnaireAdapter(
                    NinchatQuestionnaire(NinchatQuestionnaireItemGetter.getElements(questionnaireElement)), false)
            val spaceInPixelTop = itemView.resources.getDimensionPixelSize(R.dimen.ninchat_questionnaire_item_margin_start)
            val spaceLeft = 0
            val spaceRight = 0
            mRecyclerView.addItemDecoration(NinchatQuestionnaireItemDecoration(
                    spaceInPixelTop,
                    spaceLeft,
                    spaceRight
            ))
            mRecyclerView.adapter = mFormLikeAudienceQuestionnaireAdapter
            mRecyclerView.setHasFixedSize(true)
            EventBus.getDefault().post(OnItemLoaded(position))
        }, 1500)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(onComponentError: OnComponentError) {
        val name = NinchatQuestionnaireItemGetter.getName(mQuestionnaireElementWeakReference.get())
        if (TextUtils.isEmpty(name) || !name.equals(onComponentError.itemName, ignoreCase = true)) {
            return
        }
        mFormLikeAudienceQuestionnaireAdapter.notifyDataSetChanged()
        val errorIndex = NinchatQuestionnaireItemSetter.getFirstErrorIndex(mQuestionnaireElementWeakReference.get())
        mRecyclerView.clearFocus()
        mRecyclerView.scrollToPosition(errorIndex)
    }

    init {
        EventBus.getDefault().register(this)
        mTextView = itemView.findViewById(R.id.ninchat_chat_message_bot_text)
        mImageView = itemView.findViewById(R.id.ninchat_chat_message_bot_writing)
        mBotImageView = itemView.findViewById(R.id.ninchat_chat_message_bot_avatar)
        mRecyclerView = itemView.findViewById(R.id.questionnaire_conversation_rview)
        mRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
        mQuestionnaireElementWeakReference = WeakReference<Any?>(questionnaireElement)
        bind(questionnaireElement, botDetails, position)
    }
}