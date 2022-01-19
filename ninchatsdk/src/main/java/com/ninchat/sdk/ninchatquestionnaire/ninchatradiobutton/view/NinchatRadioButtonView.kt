package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.view

import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.URLUtil
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.paris.extensions.style
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.presenter.INinchatRadioButtonPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.presenter.NinchatRadioButtonPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter.OnToggleListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.view.INinchatRadioButtonListView
import kotlinx.android.synthetic.main.href_item.view.*
import kotlinx.android.synthetic.main.radio_item.view.*
import org.json.JSONObject

class NinchatRadioButtonView(
        itemView: View,
        jsonObject: JSONObject?,
        enabled: Boolean,
        private val optionToggleCallback: INinchatRadioButtonListView,
) : RecyclerView.ViewHolder(itemView), INinchatRadioButtonPresenter {

    private val presenter = NinchatRadioButtonPresenter(
            jsonObject = jsonObject,
            enabled = enabled,
            viewCallback = this)

    init {
        presenter.renderCurrentView(enabled = enabled)
        attachUserActionHandler()
    }

    fun update(isSelected: Boolean, enabled: Boolean) {
        presenter.updateCurrentView(isSelected = isSelected, enabled = enabled)
    }

    private fun attachUserActionHandler() {
        itemView.radio_button_item.setOnClickListener {
            presenter.onToggleSelection()
        }
    }

    override fun renderView(label: String, isSelected: Boolean, enabled: Boolean, isHrefElement: Boolean, hasLabel: Boolean) {
        itemView.isEnabled = enabled
        itemView.single_radio_item.visibility = if (isHrefElement) View.GONE else View.VISIBLE
        itemView.single_href_radio_item.visibility = if (isHrefElement) View.VISIBLE else View.GONE
        if (isHrefElement) {
            itemView.single_href_radio_item.style(when {
                isSelected -> R.style.NinchatTheme_Questionnaire_HyperLink_Selected
                else -> R.style.NinchatTheme_Questionnaire_HyperLink
            })
            listOf(
                    Pair(itemView.single_href_radio_item.ninchat_href_text, !isSelected && hasLabel),
                    Pair(itemView.single_href_radio_item.ninchat_href_button, !isSelected && !hasLabel),
                    Pair(itemView.single_href_radio_item.ninchat_href_text_selected, isSelected && hasLabel),
                    Pair(itemView.single_href_radio_item.ninchat_href_button_selected, isSelected && !hasLabel)).forEach {
                it.first.visibility = if (it.second) View.VISIBLE else View.GONE
                it.first.text = label
            }
        } else {
            itemView.single_radio_item.visibility = View.VISIBLE
            itemView.single_href_radio_item.visibility = View.GONE
            itemView.single_radio_item.text = label
            // render initialize view
            if (isSelected) {
                itemView.single_radio_item.style(R.style.NinchatTheme_Questionnaire_Radio_Selected)
            } else {
                itemView.single_radio_item.style(R.style.NinchatTheme_Questionnaire_Radio)
            }
        }

    }

    override fun updateView(label: String, isSelected: Boolean, enabled: Boolean, isHrefElement: Boolean, hasLabel: Boolean) {
        itemView.isEnabled = enabled
        itemView.single_radio_item.visibility = if (isHrefElement) View.GONE else View.VISIBLE
        itemView.single_href_radio_item.visibility = if (isHrefElement) View.VISIBLE else View.GONE

        if (isHrefElement) {
            itemView.single_href_radio_item.style(when {
                isSelected -> R.style.NinchatTheme_Questionnaire_HyperLink_Selected
                else -> R.style.NinchatTheme_Questionnaire_HyperLink
            })
            listOf(
                    Pair(itemView.single_href_radio_item.ninchat_href_text, !isSelected && hasLabel),
                    Pair(itemView.single_href_radio_item.ninchat_href_button, !isSelected && !hasLabel),
                    Pair(itemView.single_href_radio_item.ninchat_href_text_selected, isSelected && hasLabel),
                    Pair(itemView.single_href_radio_item.ninchat_href_button_selected, isSelected && !hasLabel)).forEach {
                it.first.visibility = if (it.second) View.VISIBLE else View.GONE
            }
        } else {
            // update view
            if (isSelected) {
                itemView.single_radio_item.style(R.style.NinchatTheme_Questionnaire_Radio_Selected)
            } else {
                itemView.single_radio_item.style(R.style.NinchatTheme_Questionnaire_Radio)
            }
        }

    }

    override fun onSelected(isHrefElement: Boolean, hasLabel: Boolean, uri: String) {
        if (isHrefElement) {
            itemView.single_href_radio_item.style(R.style.NinchatTheme_Questionnaire_HyperLink_Selected)
            listOf(
                    Pair(itemView.single_href_radio_item.ninchat_href_text, false),
                    Pair(itemView.single_href_radio_item.ninchat_href_button, false),
                    Pair(itemView.single_href_radio_item.ninchat_href_text_selected, hasLabel),
                    Pair(itemView.single_href_radio_item.ninchat_href_button_selected, !hasLabel)).forEach {
                it.first.visibility = if (it.second) View.VISIBLE else View.GONE
            }
            if (!URLUtil.isValidUrl(uri)) {
                return
            }
            ContextCompat.startActivity(itemView.context, Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(uri)
            }, null)
        }
        optionToggleCallback.onOptionToggled(isSelected = true, listPosition = layoutPosition)
    }

    override fun onUnSelected(isHrefElement: Boolean, hasLabel: Boolean) {
        if (isHrefElement) {
            itemView.single_href_radio_item.style(R.style.NinchatTheme_Questionnaire_HyperLink)
            listOf(
                    Pair(itemView.single_href_radio_item.ninchat_href_text, hasLabel),
                    Pair(itemView.single_href_radio_item.ninchat_href_button, !hasLabel),
                    Pair(itemView.single_href_radio_item.ninchat_href_text_selected, false),
                    Pair(itemView.single_href_radio_item.ninchat_href_button_selected, false)).forEach {
                it.first.visibility = if (it.second) View.VISIBLE else View.GONE
            }
        }
        optionToggleCallback.onOptionToggled(isSelected = false, listPosition = layoutPosition)
    }

}

interface INinchatRadioButtonView {
    fun onToggleSelection()
}