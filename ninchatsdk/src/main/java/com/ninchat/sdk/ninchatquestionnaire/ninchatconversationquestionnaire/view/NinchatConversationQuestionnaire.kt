package com.ninchat.sdk.ninchatquestionnaire.ninchatconversationquestionnaire.view

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.ninchatconversationquestionnaire.presenter.NinchatConversationQuestionnairePresenter

class NinchatConversationQuestionnaire(
        ninchatQuestionnaire: NinchatQuestionnaire?,
        botDetails: Pair<String?, String?>?,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), INinchatConversationQuestionnaire {

    private val presenter = NinchatConversationQuestionnairePresenter(
            questionnaire = ninchatQuestionnaire, botDetails = botDetails, viewCallback = this)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {
        val currentItem = presenter.get(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int = presenter.size()
    override fun getItemViewType(position: Int): Int = position
    override fun onRemovedElement(position: Int) {
        notifyItemChanged(position)
    }
}

interface INinchatConversationQuestionnaire {
    fun onRemovedElement(position: Int)
}