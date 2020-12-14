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
import kotlinx.android.synthetic.main.multichoice_with_label.view.*
import org.json.JSONObject

class NinchatRadioButtonListView(
        itemView: View,
        jsonObject: JSONObject?,
        isFormLikeQuestionnaire: Boolean,
        updateCallback: ButtonListUpdateListener,
        position: Int
) : RecyclerView.ViewHolder(itemView), INinchatRadioButtonListPresenter {

    private val ninchatRadioButtonList = NinchatRadioButtonListPresenter(
            jsonObject = jsonObject,
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
            viewCallback = this,
            updateCallback = updateCallback,
            position = position
    )

    fun update(jsonObject: JSONObject?) {
        ninchatRadioButtonList.renderCurrentView(jsonObject = jsonObject)
    }

    override fun onUpdateFormView(label: String, hasError: Boolean) {
        itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_chat_questionnaire_background)
        renderCommon(label = label, hasError = hasError)
    }

    override fun onUpdateConversationView(label: String, hasError: Boolean) {
        renderCommon(label = label, hasError = hasError)
    }

    private fun renderCommon(label: String, hasError: Boolean) {
        itemView.radio_option_label.text = label
        itemView.ninchat_chat_radio_options.layoutManager = LinearLayoutManager(itemView.context)
        itemView.ninchat_chat_radio_options.adapter = NinchatRadioButtonListViewAdapter()
        if (hasError) {
            itemView.radio_option_label.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_color_error_background));
        } else {
            itemView.radio_option_label.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_colorPrimary));
        }
    }

    inner class NinchatRadioButtonListViewAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>(), INinchatRadioButtonListView {
        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {
            val jsonObject = ninchatRadioButtonList.optionList()?.optJSONObject(position)
            return NinchatRadioButtonView(
                    itemView = LayoutInflater.from(parent.context).inflate(R.layout.radio_item, parent, false),
                    jsonObject = jsonObject,
                    optionToggleCallback = this,
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val jsonObject = ninchatRadioButtonList.optionList()?.optJSONObject(position)
            val isSelected = ninchatRadioButtonList.isSelected(jsonObject = jsonObject)
            (holder as NinchatRadioButtonView).update(isSelected = isSelected)
        }

        override fun getItemCount(): Int {
            return ninchatRadioButtonList.optionList()?.length() ?: 0
        }

        override fun onOptionToggled(isSelected: Boolean, listPosition: Int) {
            val previousIndex = ninchatRadioButtonList.handleOptionToggled(
                    isSelected = isSelected,
                    listPosition = listPosition)
            // if there is a last selected position
            if (previousIndex != -1) {
                notifyItemChanged(previousIndex)
            }
        }
    }
}

interface INinchatRadioButtonListView {
    fun onOptionToggled(isSelected: Boolean, listPosition: Int)
}