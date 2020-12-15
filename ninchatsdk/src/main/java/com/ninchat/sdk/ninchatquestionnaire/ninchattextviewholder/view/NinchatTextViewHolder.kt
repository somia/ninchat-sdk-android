package com.ninchat.sdk.ninchatquestionnaire.ninchattextviewholder.view

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchattextviewholder.presenter.INinchatTextViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchattextviewholder.presenter.NinchatTextViewPresenter
import com.ninchat.sdk.utils.misc.Misc
import kotlinx.android.synthetic.main.text_view.view.*
import org.json.JSONObject

class NinchatTextViewHolder(
        itemView: View,
        jsonObject: JSONObject?,
        isFormLikeQuestionnaire: Boolean = true,
        position: Int,
        enabled: Boolean
) : RecyclerView.ViewHolder(itemView), INinchatTextViewPresenter {

    private val presenter = NinchatTextViewPresenter(
            jsonObject = jsonObject,
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
            iPresenter = this,
            enabled = enabled,
            position = position
    )

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        presenter.renderCurrentView(enabled = enabled)
    }

    override fun onUpdateConversationView(label: String?, enabled: Boolean) {
        val text = Misc.toRichText(label, itemView.text_view_content)
        val textColor = if(enabled) R.color.ninchat_color_text_normal else R.color.ninchat_color_text_disabled
        if (text.isNotBlank()) {
            itemView.text_view_content.text = text
        }
        itemView.isEnabled = enabled
        itemView.text_view_content.setTextColor(ContextCompat.getColor(itemView.context, textColor))
    }

    override fun onUpdateFormView(label: String?, enabled: Boolean) {
        val text = Misc.toRichText(label, itemView.text_view_content)
        val background = R.drawable.ninchat_chat_questionnaire_background
        if (text.isNotBlank()) {
            itemView.text_view_content.text = text
        }
        itemView.background = ContextCompat.getDrawable(itemView.context, background)
        itemView.isEnabled = enabled
    }
}