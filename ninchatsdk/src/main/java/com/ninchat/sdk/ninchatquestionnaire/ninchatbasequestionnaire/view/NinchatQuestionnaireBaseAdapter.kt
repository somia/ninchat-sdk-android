package com.ninchat.sdk.ninchatquestionnaire.ninchatbasequestionnaire.view

import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire
import org.json.JSONArray
import org.json.JSONObject

abstract class NinchatQuestionnaireBaseAdapter(
        public var questionnaire: NinchatQuestionnaire?,
        protected var botDetails: Pair<String, String>,
        protected var isFormLikeQuestionnaire: Boolean,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun addContent(questionnaireList: JSONObject?) {
        questionnaire!!.addQuestionnaire(questionnaireList)
    }

    fun updateContent(questionnaireList: JSONArray?) {
        questionnaire!!.updateQuestionnaireList(questionnaireList)
    }

    val lastElement: JSONObject
        get() = questionnaire!!.lastElement
    val secondLastElement: JSONObject
        get() = questionnaire!!.secondLastElement

    fun removeLast() {
        val position = itemCount - 1
        questionnaire!!.removeQuestionnaireList(position)
        notifyItemRemoved(position)
    }

    val questionnaireList: JSONArray?
        get() = if (questionnaire == null) null else questionnaire!!.questionnaireList

    override fun getItemCount(): Int {
        return if (questionnaire == null) 0 else questionnaire!!.size()
    }
}