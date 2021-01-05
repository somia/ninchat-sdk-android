package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.view

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireType
import com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.view.NinchatBotWriting
import com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.view.NinchatButtonViewHolder
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.view.NinchatCheckboxViewHolder
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.view.NinchatDropDownSelectViewHolder
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.view.NinchatInputFieldViewHolder
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view.QuestionnaireActivityCallback
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter.INinchatConversationListPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter.NinchatConversationListPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter.NinchatFormListPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.view.NinchatRadioButtonListView
import com.ninchat.sdk.ninchatquestionnaire.ninchattextviewholder.view.NinchatTextViewHolder
import org.json.JSONObject
import kotlin.math.max

class NinchatQuestionnaireListAdapter(
        questionnaireList: List<JSONObject>,
        preAnswers: List<Pair<String, Any>>,
        isFormLike: Boolean,
        rootActivityCallback: QuestionnaireActivityCallback,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), INinchatConversationListPresenter {

    val presenter = if (isFormLike) {
        NinchatFormListPresenter(
                questionnaireList = questionnaireList,
                preAnswers = preAnswers,
                rootActivityCallback = rootActivityCallback
        )
    } else {
        NinchatConversationListPresenter(
                questionnaireList = questionnaireList,
                preAnswers = preAnswers,
                rootActivityCallback = rootActivityCallback,
                viewCallback = this
        )
    }

    init {
        presenter.init()
    }

    override fun getItemViewType(position: Int): Int = position
    override fun getItemId(position: Int): Long {
        return 1L * position
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {
        val currentElement = presenter.get(position)
        Log.e("onCreateViewHolder", "$position $itemCount")
        return when {
            NinchatQuestionnaireType.isText(currentElement) -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.text_view, parent, false)
                NinchatTextViewHolder(
                        itemView = view,
                        jsonObject = currentElement,
                        isFormLikeQuestionnaire = false,
                        position = position,
                        enabled = presenter.isLast(position)
                )
            }
            NinchatQuestionnaireType.isInput(currentElement) -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.text_field_with_label, parent, false)
                NinchatInputFieldViewHolder(
                        itemView = view,
                        jsonObject = currentElement,
                        isMultiline = NinchatQuestionnaireType.isTextArea(currentElement),
                        isFormLikeQuestionnaire = false,
                        updateCallback = presenter,
                        position = position,
                        enabled = presenter.isLast(position)
                )
            }
            NinchatQuestionnaireType.isTextArea(currentElement) -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.text_area_with_label, parent, false)
                NinchatInputFieldViewHolder(
                        itemView = view,
                        jsonObject = currentElement,
                        isMultiline = NinchatQuestionnaireType.isTextArea(currentElement),
                        isFormLikeQuestionnaire = false,
                        updateCallback = presenter,
                        position = position,
                        enabled = presenter.isLast(position)
                )
            }
            NinchatQuestionnaireType.isRadio(currentElement) -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.multichoice_with_label, parent, false)
                NinchatRadioButtonListView(
                        itemView = view,
                        jsonObject = currentElement,
                        isFormLikeQuestionnaire = false,
                        updateCallback = presenter,
                        position = position,
                        enabled = presenter.isLast(position)
                )
            }
            NinchatQuestionnaireType.isSelect(currentElement) || NinchatQuestionnaireType.isLikeRT(currentElement) -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.dropdown_with_label, parent, false)
                NinchatDropDownSelectViewHolder(
                        itemView = view,
                        jsonObject = currentElement,
                        isFormLikeQuestionnaire = false,
                        updateCallback = presenter,
                        position = position,
                        enabled = presenter.isLast(position)
                )
            }
            NinchatQuestionnaireType.isCheckBox(currentElement) -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.checkbox_simple, parent, false)
                NinchatCheckboxViewHolder(
                        itemView = view,
                        jsonObject = currentElement,
                        isFormLikeQuestionnaire = false,
                        updateCallback = presenter,
                        position = position,
                        enabled = presenter.isLast(position)
                )
            }
            NinchatQuestionnaireType.isButton(currentElement) -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.control_buttons, parent, false)
                NinchatButtonViewHolder(
                        itemView = view,
                        jsonObject = currentElement,
                        position = position,
                        enabled = presenter.isLast(position)
                )
            }
            NinchatQuestionnaireType.isBotElement(currentElement) -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.bot_item_conversation_view, parent, false)
                NinchatBotWriting(
                        itemView = view,
                        jsonObject = currentElement,
                        position = position,
                        updateCallback = presenter,
                        enabled = presenter.isLast(position)
                )
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.text_view, parent, false)
                NinchatTextViewHolder(
                        itemView = view,
                        jsonObject = currentElement,
                        isFormLikeQuestionnaire = false,
                        position = position,
                        enabled = presenter.isLast(position)
                )
            }

        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val currentElement = presenter.get(position)
        Log.e("onBindViewHolder", "$position $itemCount")
        when (viewHolder) {
            is NinchatTextViewHolder -> viewHolder.update(jsonObject = currentElement, enabled = presenter.isLast(position))
            is NinchatInputFieldViewHolder -> viewHolder.update(jsonObject = currentElement, enabled = presenter.isLast(position))
            is NinchatRadioButtonListView -> viewHolder.update(jsonObject = currentElement, enabled = presenter.isLast(position))
            is NinchatDropDownSelectViewHolder -> viewHolder.update(jsonObject = currentElement, enabled = presenter.isLast(position))
            is NinchatCheckboxViewHolder -> viewHolder.update(jsonObject = currentElement, enabled = presenter.isLast(position))
            is NinchatBotWriting -> viewHolder.update(jsonObject = currentElement, enabled = presenter.isLast(position))
            is NinchatButtonViewHolder -> viewHolder.update(jsonObject = currentElement, enabled = presenter.isLast(position))
        }
    }

    override fun getItemCount(): Int = presenter.size()

    override fun onAddItem(positionStart: Int, lastItemCount: Int) {
        val position = max(positionStart, 0)
        val totalCount = max(itemCount - position, 1)
        Log.e("onAddItem", "pos, insert, total $position $totalCount $itemCount")
        notifyItemRangeInserted(positionStart, totalCount)
        // also update the last items
        onItemUpdate(positionStart = position - lastItemCount, totalItemCount = lastItemCount)
    }

    override fun onItemRemoved(positionStart: Int, totalItemCount: Int, lastItemCount: Int) {
        val position = max(positionStart, 0)
        Log.e("onItemRemoved", "pos delete total $position $totalItemCount $itemCount")
        notifyItemRangeRemoved(position, totalItemCount)
        // also update the last items
        onItemUpdate(positionStart = position - lastItemCount, totalItemCount = lastItemCount)
    }

    override fun onItemUpdate(positionStart: Int, totalItemCount: Int) {
        val position = max(positionStart, 0)
        Log.e("onItemUpdate", "pos, update, total $position $totalItemCount $itemCount")
        notifyItemRangeChanged(position, totalItemCount)
    }

    fun showThankYou(isComplete: Boolean = false) {
        presenter.addThankYouView(isComplete)
    }

    fun showNextQuestionnaire(onNextQuestionnaire: OnNextQuestionnaire) {
        presenter.showNext(onNextQuestionnaire)
    }
}
