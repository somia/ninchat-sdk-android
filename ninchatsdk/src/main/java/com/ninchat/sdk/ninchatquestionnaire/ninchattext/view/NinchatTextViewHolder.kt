package com.ninchat.sdk.ninchatquestionnaire.ninchattext.view

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchattext.presenter.INinchatTextViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchattext.presenter.NinchatTextViewPresenter
import com.ninchat.sdk.utils.misc.Misc
import kotlinx.android.synthetic.main.text_view.view.*
import org.json.JSONObject

class NinchatTextViewHolder(
        itemView: View,
        jsonObject: JSONObject?,
        isFormLikeQuestionnaire: Boolean = true,
) : RecyclerView.ViewHolder(itemView), INinchatTextViewPresenter {

    private val ninchatTextViewPresenter = NinchatTextViewPresenter(
            jsonObject = jsonObject,
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
            iPresenter = this
    )

    fun update(jsonObject: JSONObject?, isFormLikeQuestionnaire: Boolean = true) {
        ninchatTextViewPresenter.renderCurrentView()
    }

    override fun onUpdateConversationView(label: String?) {
        itemView.text_view_content.text = Misc.toRichText(label, itemView.text_view_content)
    }

    override fun onUpdateFormView(label: String?) {
        val background = R.drawable.ninchat_chat_questionnaire_background
        itemView.text_view_content.text = Misc.toRichText(label, itemView.text_view_content)
        itemView.background = ContextCompat.getDrawable(itemView.context, background)
    }
}