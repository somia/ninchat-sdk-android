package com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.presenter.INinchatBotWritingViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.presenter.NinchatBotWritingViewPresenter
import com.ninchat.sdk.utils.misc.Misc
import kotlinx.android.synthetic.main.bot_item_conversation_view.view.*
import org.json.JSONObject

class NinchatBotWriting(
        itemView: View,
        jsonObject: JSONObject?,
        position: Int,
        enabled: Boolean,
) : RecyclerView.ViewHolder(itemView), INinchatBotWritingViewPresenter {
    private val presenter = NinchatBotWritingViewPresenter(
            jsonObject = jsonObject,
            position = position,
            enabled = enabled,
            presenter = this
    )

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        presenter.updateModel(jsonObject = jsonObject, enabled = enabled)
        presenter.renderCurrentView()
    }

    override fun onUpdateView(label: String?, imgUrl: String?, enabled: Boolean) {
        val text = Misc.toRichText(label, itemView.ninchat_chat_message_bot_text)
//        val textColor = if(enabled) R.color.ninchat_color_text_normal else R.color.ninchat_color_text_disabled
        if (text.isNotBlank()) {
            itemView.ninchat_chat_message_bot_text.text = text
        }
        itemView.isEnabled = enabled
//        itemView.ninchat_chat_message_bot_text.setTextColor(ContextCompat.getColor(itemView.context, textColor))
    }

}