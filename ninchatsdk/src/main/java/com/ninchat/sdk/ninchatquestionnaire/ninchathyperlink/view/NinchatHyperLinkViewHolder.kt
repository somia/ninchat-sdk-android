package com.ninchat.sdk.ninchatquestionnaire.ninchathyperlink.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.paris.extensions.style
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchathyperlink.presenter.HyperLinkClickListener
import com.ninchat.sdk.ninchatquestionnaire.ninchathyperlink.presenter.INinchatHyperLinkPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchathyperlink.presenter.NinchatHyperLinkViewPresenter
import kotlinx.android.synthetic.main.href_item.view.*
import org.json.JSONObject
import androidx.core.content.ContextCompat.startActivity

import android.content.Intent
import android.net.Uri
import android.webkit.URLUtil


class NinchatHyperLinkViewHolder(
        itemView: View,
        jsonObject: JSONObject?,
        position: Int,
        hyperLinkClickListener: HyperLinkClickListener,
        enabled: Boolean,
) : RecyclerView.ViewHolder(itemView), INinchatHyperLinkPresenter {
    private val presenter = NinchatHyperLinkViewPresenter(
            jsonObject = jsonObject,
            position = position,
            enabled = enabled,
            viewCallback = this,
            updateCallback = hyperLinkClickListener,
    )

    init {
        presenter.renderCurrentView()
        attachHandler()
    }

    private fun attachHandler() {
        itemView.setOnClickListener {
            presenter.onLinkClicked()
        }
    }

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        presenter.apply {
            updateCurrentView(jsonObject = jsonObject, enabled = enabled)
        }
    }

    override fun onRenderView(label: String, isSelected: Boolean, enabled: Boolean, hasLabel: Boolean) {
        itemView.isEnabled = enabled
        itemView.ninchat_href_item.style(when {
            !enabled -> R.style.NinchatTheme_Questionnaire_HyperLink_Unfocused
            isSelected -> R.style.NinchatTheme_Questionnaire_HyperLink_Selected
            else -> R.style.NinchatTheme_Questionnaire_HyperLink

        })
        listOf(
                Pair(itemView.ninchat_href_item.ninchat_href_text, !isSelected && hasLabel),
                Pair(itemView.ninchat_href_item.ninchat_href_button, !isSelected && !hasLabel),
                Pair(itemView.ninchat_href_item.ninchat_href_text_selected, isSelected && hasLabel),
                Pair(itemView.ninchat_href_item.ninchat_href_button_selected, isSelected && !hasLabel)).forEach {
            it.first.visibility = if (it.second) View.VISIBLE else View.GONE
            it.first.text = label
        }
    }

    override fun onUpdateView(isSelected: Boolean, enabled: Boolean, hasLabel: Boolean) {
        itemView.isEnabled = enabled
        itemView.ninchat_href_item.style(when {
            !enabled -> R.style.NinchatTheme_Questionnaire_HyperLink_Unfocused
            isSelected -> R.style.NinchatTheme_Questionnaire_HyperLink_Selected
            else -> R.style.NinchatTheme_Questionnaire_HyperLink

        })
        listOf(
                Pair(itemView.ninchat_href_item.ninchat_href_text, !isSelected && hasLabel),
                Pair(itemView.ninchat_href_item.ninchat_href_button, !isSelected && !hasLabel),
                Pair(itemView.ninchat_href_item.ninchat_href_text_selected, isSelected && hasLabel),
                Pair(itemView.ninchat_href_item.ninchat_href_button_selected, isSelected && !hasLabel)).forEach {
            it.first.visibility = if (it.second) View.VISIBLE else View.GONE
        }
    }

    override fun onClickedView(hasLabel: Boolean, uri: String) {
        itemView.ninchat_href_item.style(R.style.NinchatTheme_Questionnaire_HyperLink_Selected)
        listOf(
                Pair(itemView.ninchat_href_item.ninchat_href_text, false),
                Pair(itemView.ninchat_href_item.ninchat_href_button, false),
                Pair(itemView.ninchat_href_item.ninchat_href_text_selected, hasLabel),
                Pair(itemView.ninchat_href_item.ninchat_href_button_selected, !hasLabel)).forEach {
            it.first.visibility = if (it.second) View.VISIBLE else View.GONE
        }

        if (!URLUtil.isValidUrl(uri)) {
            return
        }
        startActivity(itemView.context, Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(uri)
        }, null)

    }
}