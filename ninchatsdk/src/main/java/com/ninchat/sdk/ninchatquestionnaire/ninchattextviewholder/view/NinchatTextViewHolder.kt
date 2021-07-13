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
        enabled: Boolean,
) : RecyclerView.ViewHolder(itemView), INinchatTextViewPresenter {

    private val presenter = NinchatTextViewPresenter(
            jsonObject = jsonObject,
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
            iPresenter = this,
            enabled = enabled,
            position = position
    )

    init {
        presenter.renderCurrentView(enabled = enabled)
    }

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        presenter.updateCurrentView(enabled = enabled)
    }

    override fun onUpdateConversationView(label: String?, enabled: Boolean) {
        itemView.isEnabled = enabled
        itemView.text_view_content.setTextAppearance(if (enabled) R.style.NinchatTheme_Questionnaire_TextView else R.style.NinchatTheme_Questionnaire_TextView_Disabled)
    }

    override fun onRenderConversationVIew(label: String?, enabled: Boolean) {
        val text = Misc.toRichText(label, itemView.text_view_content)
        if (text.isNotBlank()) {
            itemView.text_view_content.text = text
        }
        itemView.text_view_content.setTextAppearance(if (enabled) R.style.NinchatTheme_Questionnaire_TextView else R.style.NinchatTheme_Questionnaire_TextView_Disabled)
        itemView.isEnabled = enabled
    }

    override fun onUpdateFormView(label: String?, enabled: Boolean) {
        itemView.isEnabled = enabled
    }

    override fun onRenderFormView(label: String?, enabled: Boolean) {
        val text = Misc.toRichText(label, itemView.text_view_content)
        if (text.isNotBlank()) {
            itemView.text_view_content.text = text
        }
        itemView.text_view_content.setTextAppearance(if (enabled) R.style.NinchatTheme_Questionnaire_TextView else R.style.NinchatTheme_Questionnaire_TextView_Disabled)
        itemView.isEnabled = enabled
    }
}