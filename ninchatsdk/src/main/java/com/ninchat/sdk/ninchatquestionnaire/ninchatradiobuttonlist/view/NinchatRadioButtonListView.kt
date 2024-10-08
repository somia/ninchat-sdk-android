package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.paris.extensions.style
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.view.NinchatRadioButtonView
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter.ButtonListUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter.INinchatRadioButtonListPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter.NinchatRadioButtonListPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter.OnToggleListener
import com.ninchat.sdk.utils.misc.Misc
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

    private val toggleLister = OnToggleListener(intervalInMs = 200)

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        presenter.updateCurrentView(jsonObject = jsonObject, enabled = enabled)
    }

    override fun renderCommon(
        label: String,
        hasError: Boolean,
        enabled: Boolean,
        isFormLike: Boolean
    ) {
        itemView.isEnabled = enabled
        itemView.radio_option_label.text = Misc.toRichText(label, itemView.radio_option_label)
        if (label.isNullOrEmpty()) itemView.radio_option_label.visibility = View.GONE
        itemView.ninchat_chat_radio_options.apply {
            layoutManager = LinearLayoutManager(itemView.context)
            adapter = NinchatRadioButtonListViewAdapter()
        }
        if (hasError) {
            itemView.radio_option_label.style(
                if (isFormLike) R.style.NinchatTheme_Questionnaire_Label_Form_Error else R.style.NinchatTheme_Questionnaire_Label_Error
            )
        } else {
            itemView.radio_option_label.style(
                if (enabled) if (isFormLike) R.style.NinchatTheme_Questionnaire_Label_Form else R.style.NinchatTheme_Questionnaire_Label
                else if (isFormLike) R.style.NinchatTheme_Questionnaire_Label_Form_Disabled else R.style.NinchatTheme_Questionnaire_Label_Disabled
            )

        }
    }

    override fun updateCommon(
        label: String,
        hasError: Boolean,
        enabled: Boolean,
        isFormLike: Boolean
    ) {
        itemView.isEnabled = enabled
        (itemView.ninchat_chat_radio_options.adapter as NinchatRadioButtonListViewAdapter).notifyDataSetChanged()
        if (label.isNullOrEmpty()) itemView.radio_option_label.visibility = View.GONE
        if (hasError) {
            itemView.radio_option_label.style(
                if (isFormLike) R.style.NinchatTheme_Questionnaire_Label_Form_Error else R.style.NinchatTheme_Questionnaire_Label_Error
            )
        } else {
            itemView.radio_option_label.style(
                if (enabled) if (isFormLike) R.style.NinchatTheme_Questionnaire_Label_Form else R.style.NinchatTheme_Questionnaire_Label
                else if (isFormLike) R.style.NinchatTheme_Questionnaire_Label_Form_Disabled else R.style.NinchatTheme_Questionnaire_Label_Disabled
            )
        }
    }

    inner class NinchatRadioButtonListViewAdapter() :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(), INinchatRadioButtonListView {
        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {
            val jsonObject = presenter.optionList()?.optJSONObject(position)
            return NinchatRadioButtonView(
                itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.radio_item, parent, false),
                jsonObject = jsonObject,
                optionToggleCallback = this,
                enabled = presenter.isEnabled()
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val isSelected = presenter.isSelected(currentPosition = position)
            (holder as NinchatRadioButtonView).update(
                isSelected = isSelected,
                enabled = presenter.isEnabled()
            )
        }

        override fun getItemCount(): Int {
            return presenter.optionList()?.length() ?: 0
        }

        override fun onOptionToggled(isSelected: Boolean, listPosition: Int) {
            toggleLister.onButtonToggle(callback = {
                // update view
                itemView.radio_option_label.setTextAppearance(R.style.NinchatTheme_Questionnaire_Label)
                val previousIndex = presenter.handleOptionToggled(
                    isSelected = isSelected,
                    listPosition = listPosition
                )
                // if there is a last selected position
                if (previousIndex != -1) {
                    notifyItemChanged(previousIndex)
                }
                notifyItemChanged(listPosition)
            })

        }
    }
}

interface INinchatRadioButtonListView {
    fun onOptionToggled(isSelected: Boolean, listPosition: Int)
}