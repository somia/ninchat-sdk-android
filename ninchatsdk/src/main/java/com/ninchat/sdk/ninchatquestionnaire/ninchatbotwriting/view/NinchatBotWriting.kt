package com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.view

import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.presenter.INinchatBotWritingViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.presenter.NinchatBotWritingViewPresenter
import kotlinx.android.synthetic.main.bot_item_conversation_view.view.*
import kotlinx.android.synthetic.main.bot_writing_indicator.view.*
import org.json.JSONObject

class NinchatBotWriting(
        itemView: View,
        jsonObject: JSONObject?,
        position: Int,
        updateCallback: ((Int) -> Unit)?,
        enabled: Boolean,
) : RecyclerView.ViewHolder(itemView), INinchatBotWritingViewPresenter {
    private val presenter = NinchatBotWritingViewPresenter(
            jsonObject = jsonObject,
            position = position,
            enabled = enabled,
            updateCallback = updateCallback,
            presenter = this
    )

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        presenter.updateModel(jsonObject = jsonObject, enabled = enabled)
        presenter.renderCurrentView()
    }

    override fun onUpdateView(label: String?, imgUrl: String?, enabled: Boolean) {
        itemView.ninchat_chat_message_bot_text.text = label
        itemView.isEnabled = enabled
        // is already loaded
        if (presenter.isLoaded()) {
            itemView.ninchat_chat_message_bot_writing_root.visibility = View.GONE
            return
        }

        itemView.ninchat_chat_message_bot_writing.setBackgroundResource(R.drawable.ninchat_icon_chat_writing_indicator)
        val animationDrawable = (itemView.ninchat_chat_message_bot_writing.background) as AnimationDrawable
        animationDrawable.start()

        Handler().postDelayed({
            animationDrawable.stop()
            itemView.ninchat_chat_message_bot_writing_root.visibility = View.GONE
            presenter.onAnimationComplete()
        }, 1500)
    }

}