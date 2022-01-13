package com.ninchat.sdk.ninchatquestionnaire.ninchathyperlink.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.paris.extensions.style
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchathyperlink.presenter.INinchatHyperLinkPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchathyperlink.presenter.NinchatHyperLinkViewPresenter
import kotlinx.android.synthetic.main.href_item.view.*
import org.json.JSONObject

class NinchatHyperLinkViewHolder(
        itemView: View,
        jsonObject: JSONObject?,
        position: Int,
        enabled: Boolean,
) : RecyclerView.ViewHolder(itemView), INinchatHyperLinkPresenter {
    private val presenter = NinchatHyperLinkViewPresenter(
            jsonObject = jsonObject,
            position = position,
            enabled = enabled,
            viewCallback = this,
    )

    init {
        presenter.renderCurrentView()
    }

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        presenter.apply {
            updateCurrentView(jsonObject = jsonObject, enabled = enabled)
        }
    }

    override fun onRenderView(label: String, isSelected: Boolean, enabled: Boolean) {
        itemView.href_item.text = label
        itemView.isEnabled = enabled
        if (isSelected) {
            itemView.href_item.style(R.style.NinchatTheme_Questionnaire_Radio_Selected)
        } else {
            itemView.href_item.style(R.style.NinchatTheme_Questionnaire_Radio)
        }
    }

    override fun onUpdateView(isSelected: Boolean, enabled: Boolean) {
        itemView.isEnabled = enabled
        if (isSelected) {
            itemView.href_item.style(R.style.NinchatTheme_Questionnaire_Radio_Selected)
        } else {
            itemView.href_item.style(R.style.NinchatTheme_Questionnaire_Radio)
        }
    }
}