package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.view.NinchatRadioButtonView
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter.ButtonListUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter.INinchatRadioButtonListPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter.NinchatRadioButtonListPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter.OnToggleListener
import kotlinx.android.synthetic.main.multichoice_with_label.view.*
import org.json.JSONObject

class NinchatRadioButtonListView(
        itemView: View,
        jsonObject: JSONObject?,
        isFormLikeQuestionnaire: Boolean,
        updateCallback: ButtonListUpdateListener,
        position: Int,
        enabled: Boolean,
) : RecyclerView.ViewHolder(itemView), INinchatRadioButtonListPresenter {

    private val presenter = NinchatRadioButtonListPresenter(
            jsonObject = jsonObject,
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
            viewCallback = this,
            updateCallback = updateCallback,
            position = position,
            enabled = enabled
    )

    init {
        presenter.renderCurrentView(jsonObject = jsonObject, enabled = enabled)
    }

    private val toggleLister = OnToggleListener(intervalInMs = 500)

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        presenter.updateCurrentView(jsonObject = jsonObject, enabled = enabled)
    }

    override fun onRenderFormView(label: String, hasError: Boolean, enabled: Boolean) {
        itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_chat_questionnaire_background)
        renderCommon(label = label, hasError = hasError, enabled = enabled)
    }

    override fun onRenderConversationView(label: String, hasError: Boolean, enabled: Boolean) {
        itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_chat_questionnaire_background)
        renderCommon(label = label, hasError = hasError, enabled = enabled)
    }

    override fun onUpdateFormView(label: String, hasError: Boolean, enabled: Boolean) {
        updateCommon(label = label, hasError = hasError, enabled = enabled)
    }

    override fun onUpdateConversationView(label: String, hasError: Boolean, enabled: Boolean) {
        updateCommon(label = label, hasError = hasError, enabled = enabled)
    }

    private fun renderCommon(label: String, hasError: Boolean, enabled: Boolean) {
        itemView.isEnabled = enabled
        itemView.radio_option_label.text = label
        itemView.ninchat_chat_radio_options.apply {
            layoutManager = LinearLayoutManager(itemView.context)
            adapter = NinchatRadioButtonListViewAdapter()
        }
        if (hasError) {
            itemView.radio_option_label.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_color_error_background));
        } else {
            itemView.radio_option_label.setTextColor(ContextCompat.getColor(itemView.context, if (enabled) R.color.ninchat_color_text_normal else R.color.ninchat_color_text_disabled))
        }
    }

    private fun updateCommon(label: String, hasError: Boolean, enabled: Boolean) {
        itemView.isEnabled = enabled
        (itemView.ninchat_chat_radio_options.adapter as NinchatRadioButtonListViewAdapter).notifyDataSetChanged()
        if (hasError) {
            itemView.radio_option_label.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_color_error_background));
        } else {
            itemView.radio_option_label.setTextColor(ContextCompat.getColor(itemView.context, if (enabled) R.color.ninchat_color_text_normal else R.color.ninchat_color_text_disabled))
        }
    }

    inner class NinchatRadioButtonListViewAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>(), INinchatRadioButtonListView {
        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {
            val jsonObject = presenter.optionList()?.optJSONObject(position)
            return NinchatRadioButtonView(
                    itemView = LayoutInflater.from(parent.context).inflate(R.layout.radio_item, parent, false),
                    jsonObject = jsonObject,
                    optionToggleCallback = this,
                    enabled = presenter.isEnabled()
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val isSelected = presenter.isSelected(currentPosition = position)
            (holder as NinchatRadioButtonView).update(isSelected = isSelected, enabled = presenter.isEnabled())
        }

        override fun getItemCount(): Int {
            return presenter.optionList()?.length() ?: 0
        }

        override fun onOptionToggled(isSelected: Boolean, listPosition: Int) {
            toggleLister.onButtonToggle(callback = {
                // update view
                itemView.radio_option_label.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_color_text_normal))
                val previousIndex = presenter.handleOptionToggled(
                        isSelected = isSelected,
                        listPosition = listPosition)
                // if there is a last selected position
                if (previousIndex != -1) {
                    notifyItemChanged(previousIndex)
                }
            })

        }
    }
}

interface INinchatRadioButtonListView {
    fun onOptionToggled(isSelected: Boolean, listPosition: Int)
}