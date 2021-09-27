package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireType
import com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.view.NinchatBotWriting
import com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.view.NinchatButtonViewHolder
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckboxlist.view.NinchatCheckBoxListView
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.view.NinchatDropDownSelectViewHolder
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.view.NinchatInputFieldViewHolder
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view.QuestionnaireActivityCallback
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter.INinchatConversationListPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter.NinchatConversationListPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter.NinchatFormListPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.view.NinchatRadioButtonListView
import com.ninchat.sdk.ninchatquestionnaire.ninchattextviewholder.view.NinchatTextViewHolder
import org.json.JSONObject
import java.util.*
import kotlin.math.max

class NinchatQuestionnaireListAdapter(
    questionnaireList: List<JSONObject>,
    preAnswers: List<Pair<String, Any>>,
    val isFormLike: Boolean,
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

    override fun getItemViewType(position: Int): Int {
        return presenter.getByMuskedPosition(position).optInt("uuid")
    }

    override fun getItemId(position: Int): Long {
        return 1L * presenter.getByMuskedPosition(position)
            .optInt("uuid", UUID.randomUUID().hashCode())
    }

    override fun onCreateViewHolder(parent: ViewGroup, uuid: Int): RecyclerView.ViewHolder {
        val position = presenter.getIndexByUuid(uuid = uuid)
        val currentElement = presenter.getByIndexPosition(position)
        return when {
            NinchatQuestionnaireType.isText(currentElement) -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.text_view, parent, false)
                NinchatTextViewHolder(
                    itemView = view,
                    jsonObject = currentElement,
                    isFormLikeQuestionnaire = isFormLike,
                    position = position,
                    enabled = presenter.isLast(position)
                )
            }
            NinchatQuestionnaireType.isInput(currentElement) -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.text_field_with_label, parent, false)
                NinchatInputFieldViewHolder(
                    itemView = view,
                    jsonObject = currentElement,
                    isMultiline = NinchatQuestionnaireType.isTextArea(currentElement),
                    isFormLikeQuestionnaire = isFormLike,
                    updateCallback = presenter,
                    position = position,
                    enabled = presenter.isLast(position)
                )
            }
            NinchatQuestionnaireType.isTextArea(currentElement) -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.text_area_with_label, parent, false)
                NinchatInputFieldViewHolder(
                    itemView = view,
                    jsonObject = currentElement,
                    isMultiline = NinchatQuestionnaireType.isTextArea(currentElement),
                    isFormLikeQuestionnaire = isFormLike,
                    updateCallback = presenter,
                    position = position,
                    enabled = presenter.isLast(position)
                )
            }
            NinchatQuestionnaireType.isRadio(currentElement) -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.multichoice_with_label, parent, false)
                NinchatRadioButtonListView(
                    itemView = view,
                    jsonObject = currentElement,
                    isFormLikeQuestionnaire = isFormLike,
                    updateCallback = presenter,
                    position = position,
                    enabled = presenter.isLast(position)
                )
            }
            NinchatQuestionnaireType.isSelect(currentElement) || NinchatQuestionnaireType.isLikeRT(
                currentElement
            ) -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.dropdown_with_label, parent, false)
                NinchatDropDownSelectViewHolder(
                    itemView = view,
                    jsonObject = currentElement,
                    isFormLikeQuestionnaire = isFormLike,
                    updateCallback = presenter,
                    position = position,
                    enabled = presenter.isLast(position)
                )
            }
            NinchatQuestionnaireType.isCheckBox(currentElement) -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.checkbox_compound, parent, false)
                NinchatCheckBoxListView(
                    itemView = view,
                    jsonObject = currentElement,
                    isFormLikeQuestionnaire = isFormLike,
                    position = position,
                    updateCallback = presenter,
                    enabled = presenter.isLast(position)
                )
            }
            NinchatQuestionnaireType.isButton(currentElement) -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.control_buttons, parent, false)
                NinchatButtonViewHolder(
                    itemView = view,
                    jsonObject = currentElement,
                    position = position,
                    enabled = presenter.isLast(position)
                )
            }
            NinchatQuestionnaireType.isBotElement(currentElement) -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.bot_item_conversation_view, parent, false)
                NinchatBotWriting(
                    itemView = view,
                    jsonObject = currentElement,
                    position = position,
                    updateCallback = presenter,
                    enabled = presenter.isLast(position)
                )
            }
            else -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.text_view, parent, false)
                NinchatTextViewHolder(
                    itemView = view,
                    jsonObject = currentElement,
                    isFormLikeQuestionnaire = isFormLike,
                    position = position,
                    enabled = presenter.isLast(position)
                )
            }

        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val currentElement = presenter.getByMuskedPosition(position)
        when (viewHolder) {
            is NinchatTextViewHolder -> viewHolder.update(
                jsonObject = currentElement,
                enabled = presenter.isLast(position)
            )
            is NinchatInputFieldViewHolder -> viewHolder.update(
                jsonObject = currentElement,
                enabled = presenter.isLast(position)
            )
            is NinchatRadioButtonListView -> viewHolder.update(
                jsonObject = currentElement,
                enabled = presenter.isLast(position)
            )
            is NinchatDropDownSelectViewHolder -> viewHolder.update(
                jsonObject = currentElement,
                enabled = presenter.isLast(position)
            )
            is NinchatCheckBoxListView -> viewHolder.update(
                jsonObject = currentElement,
                enabled = presenter.isLast(position)
            )
            is NinchatBotWriting -> viewHolder.update(
                jsonObject = currentElement,
                enabled = presenter.isLast(position)
            )
            is NinchatButtonViewHolder -> viewHolder.update(
                jsonObject = currentElement,
                enabled = presenter.isLast(position)
            )
        }
    }

    override fun getItemCount(): Int = presenter.size()

    override fun onAddItem(positionStart: Int, lastItemCount: Int) {
        val position = max(positionStart, 0)
        val totalCount = max(presenter.size() - position, 1)
        notifyItemRangeInserted(positionStart, totalCount)
        // also update the last items
        onItemUpdate(positionStart = position - lastItemCount, totalItemCount = lastItemCount)
    }

    override fun onItemRemoved(positionStart: Int, totalItemCount: Int, lastItemCount: Int) {
        val position = max(positionStart, 0)
        notifyItemRangeRemoved(position, totalItemCount)
        // also update the last items
        onItemUpdate(positionStart = position - lastItemCount, totalItemCount = lastItemCount)
    }

    override fun onItemUpdate(positionStart: Int, totalItemCount: Int) {
        val position = max(positionStart, 0)
        notifyItemRangeChanged(position, totalItemCount)
    }

    fun showThankYou(isComplete: Boolean = false) {
        presenter.addThankYouView(isComplete)
    }

    fun showNextQuestionnaire(onNextQuestionnaire: OnNextQuestionnaire) {
        presenter.showNext(onNextQuestionnaire)
    }

    fun isLastElement(position: Int): Boolean = position + 2 >= presenter.size()
}
