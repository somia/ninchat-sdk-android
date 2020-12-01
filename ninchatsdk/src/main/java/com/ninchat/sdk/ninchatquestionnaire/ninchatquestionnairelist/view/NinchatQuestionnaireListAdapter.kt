package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireType
import com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.view.NinchatButtonViewHolder
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.view.NinchatCheckboxViewHolder
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.model.NinchatDropDownSelectViewModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.view.NinchatDropDownSelectViewHolder
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfield.view.NinchatInputFieldViewHolder
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter.INinchatQuestionnaireListPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter.NinchatQuestionnaireListPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.view.NinchatRadioButtonView
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.view.NinchatRadioButtonListView
import com.ninchat.sdk.ninchatquestionnaire.ninchattextviewholder.view.NinchatTextViewHolder
import com.ninchat.sdk.ninchatqueuelist.view.NinchatCloseQueue
import org.json.JSONObject

class NinchatQuestionnaireListAdapter(
        questionnaireList: List<JSONObject>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), INinchatQuestionnaireListPresenter {
    val presenter = NinchatQuestionnaireListPresenter(
            questionnaireList = questionnaireList,
            viewCallback = this
    )

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {
        val currentElement = presenter.get(position)
        val view = when {
            NinchatQuestionnaireType.isText(currentElement) -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.text_view, parent, false)
                NinchatTextViewHolder(
                        itemView = view,
                        jsonObject = currentElement,
                        isFormLikeQuestionnaire = false)
            }
            NinchatQuestionnaireType.isInput(currentElement)-> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.text_field_with_label, parent, false)
                NinchatInputFieldViewHolder(
                        itemView = view,
                        jsonObject = currentElement,
                        isMultiline = NinchatQuestionnaireType.isTextArea(currentElement),
                        isFormLikeQuestionnaire = false)
            }
            NinchatQuestionnaireType.isTextArea(currentElement) -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.text_area_with_label, parent, false)
                NinchatInputFieldViewHolder(
                        itemView = view,
                        jsonObject = currentElement,
                        isMultiline = NinchatQuestionnaireType.isTextArea(currentElement),
                        isFormLikeQuestionnaire = false)
            }
            NinchatQuestionnaireType.isRadio(currentElement) -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.multichoice_with_label, parent, false)
                NinchatRadioButtonListView(
                        itemView = view,
                        jsonObject = currentElement,
                        isFormLikeQuestionnaire = false)
            }
            NinchatQuestionnaireType.isSelect(currentElement) || NinchatQuestionnaireType.isLikeRT(currentElement) -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.dropdown_with_label, parent, false)
                NinchatDropDownSelectViewHolder(
                        itemView = view,
                        jsonObject = currentElement,
                        isFormLikeQuestionnaire = false)
            }
            NinchatQuestionnaireType.isCheckBox(currentElement) -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.checkbox_simple, parent, false)
                NinchatCheckboxViewHolder(
                        itemView = view,
                        jsonObject = currentElement,
                        isFormLikeQuestionnaire = false)
            }
            NinchatQuestionnaireType.isButton(currentElement) -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.control_buttons, parent, false)
                NinchatButtonViewHolder(
                        itemView = view,
                        jsonObject = currentElement)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.text_field_with_label, parent, false)
                NinchatTextViewHolder(
                        itemView = view,
                        jsonObject = currentElement,
                        isFormLikeQuestionnaire = false)
            }

        }
        return view
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val currentElement = presenter.get(position)
        when (viewHolder) {
            is NinchatTextViewHolder -> viewHolder.update(jsonObject = currentElement)
            is NinchatInputFieldViewHolder -> viewHolder.update(jsonObject = currentElement)
            is NinchatRadioButtonListView -> viewHolder.update(jsonObject = currentElement)
            is NinchatDropDownSelectViewHolder -> viewHolder.update(jsonObject = currentElement)
            is NinchatCheckboxViewHolder -> viewHolder.update(jsonObject = currentElement)
            is NinchatButtonViewHolder -> viewHolder.update(jsonObject = currentElement)
        }
    }

    override fun getItemCount(): Int = presenter.size()

    override fun onAddItem(positionStart: Int, itemCount: Int) {
        notifyItemRangeInserted(positionStart, itemCount)
    }
}