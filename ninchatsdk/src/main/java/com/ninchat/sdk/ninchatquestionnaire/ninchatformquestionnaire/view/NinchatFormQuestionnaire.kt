package com.ninchat.sdk.ninchatquestionnaire.ninchatformquestionnaire.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.view.NinchatButtonViewHolder
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.view.NinchatCheckboxViewHolder
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.view.NinchatDropDownSelectViewHolder
import com.ninchat.sdk.ninchatquestionnaire.ninchatformquestionnaire.presenter.NinchatFormQuestionnairePresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfield.view.NinchatInputFieldViewHolder
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.view.NinchatRadioButtonListView
import com.ninchat.sdk.ninchatquestionnaire.ninchattext.view.NinchatTextViewHolder

class NinchatFormQuestionnaire(
        ninchatQuestionnaire: NinchatQuestionnaire?,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), INinchatFormQuestionnaire {

    private val presenter = NinchatFormQuestionnairePresenter(questionnaire = ninchatQuestionnaire, viewCallback = this)
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {
        val currentItem = presenter.get(position)
        when (NinchatQuestionnaireTypeUtil.getItemType(currentItem)) {
            NinchatQuestionnaireTypeUtil.TEXT -> return NinchatTextViewHolder(
                    itemView = LayoutInflater.from(parent.context).inflate(R.layout.text_view, parent, false),
                    jsonObject = currentItem,
                    isFormLikeQuestionnaire = true)
            NinchatQuestionnaireTypeUtil.INPUT -> return NinchatInputFieldViewHolder(
                    itemView = LayoutInflater.from(parent.context).inflate(R.layout.text_field_with_label, parent, false),
                    jsonObject = currentItem,
                    isMultiline = false,
                    isFormLikeQuestionnaire = true)
            NinchatQuestionnaireTypeUtil.TEXT_AREA -> return NinchatInputFieldViewHolder(
                    itemView = LayoutInflater.from(parent.context).inflate(R.layout.text_area_with_label, parent, false),
                    jsonObject = currentItem,
                    isMultiline = true,
                    isFormLikeQuestionnaire = true)
            NinchatQuestionnaireTypeUtil.RADIO -> return NinchatRadioButtonListView(
                    itemView = LayoutInflater.from(parent.context).inflate(R.layout.multichoice_with_label, parent, false),
                    jsonObject = currentItem,
                    isFormLikeQuestionnaire = true)
            NinchatQuestionnaireTypeUtil.SELECT, NinchatQuestionnaireTypeUtil.LIKERT -> return NinchatDropDownSelectViewHolder(
                    itemView = LayoutInflater.from(parent.context).inflate(R.layout.dropdown_with_label, parent, false),
                    jsonObject = currentItem,
                    isFormLikeQuestionnaire = true)
            NinchatQuestionnaireTypeUtil.CHECKBOX -> return NinchatCheckboxViewHolder(
                    itemView = LayoutInflater.from(parent.context).inflate(R.layout.checkbox_simple, parent, false),
                    jsonObject = currentItem,
                    isFormLikeQuestionnaire = true)
            NinchatQuestionnaireTypeUtil.BUTTON -> return NinchatButtonViewHolder(
                    itemView = LayoutInflater.from(parent.context).inflate(R.layout.control_buttons, parent, false),
                    jsonObject = currentItem)
        }
        return NinchatTextViewHolder(
                itemView = LayoutInflater.from(parent.context).inflate(R.layout.text_view, parent, false),
                jsonObject = currentItem,
                isFormLikeQuestionnaire = true)

    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = presenter.get(position)
        if (viewHolder is NinchatTextViewHolder) {
            viewHolder.update(currentItem, true)
        } else if (viewHolder is NinchatInputFieldViewHolder) {
            viewHolder.update(currentItem, true)
        } else if (viewHolder is NinchatDropDownSelectViewHolder) {
            viewHolder.update(currentItem, true)
        } else if (viewHolder is NinchatRadioButtonListView) {
            viewHolder.update(currentItem)
        } else if (viewHolder is NinchatCheckboxViewHolder) {
            viewHolder.update(currentItem, true)
        } else if (viewHolder is NinchatButtonViewHolder) {
            viewHolder.update(currentItem)
        }
    }

    override fun getItemCount(): Int = presenter.size()
    override fun getItemViewType(position: Int): Int = position
    override fun onRemovedElement(position: Int) {
        notifyItemChanged(position)
    }
}

interface INinchatFormQuestionnaire {
    fun onRemovedElement(position: Int)
}