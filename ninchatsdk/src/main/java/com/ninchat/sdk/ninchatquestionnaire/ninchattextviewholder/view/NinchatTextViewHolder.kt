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
) : RecyclerView.ViewHolder(itemView), INinchatTextViewPresenter {

    private val presenter = NinchatTextViewPresenter(
            jsonObject = jsonObject,
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
            iPresenter = this
    )

    fun update(jsonObject: JSONObject?) {
        presenter.renderCurrentView()
    }

    override fun onUpdateConversationView(label: String?) {
        val text = Misc.toRichText(label, itemView.text_view_content)
        if (text.isNotBlank()) {
            itemView.text_view_content.text = text
        }
    }

    override fun onUpdateFormView(label: String?) {
        val text = Misc.toRichText(label, itemView.text_view_content)
        val background = R.drawable.ninchat_chat_questionnaire_background
        if (text.isNotBlank()) {
            itemView.text_view_content.text = text
        }
        itemView.background = ContextCompat.getDrawable(itemView.context, background)
    }
}