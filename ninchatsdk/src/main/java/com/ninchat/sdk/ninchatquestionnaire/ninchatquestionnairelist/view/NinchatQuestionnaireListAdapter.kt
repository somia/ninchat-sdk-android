package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.view

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter.NinchatQuestionnaireListPresenter

class NinchatQuestionnaireListAdapter(
        questionnaireType: Int,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val presenter = NinchatQuestionnaireListPresenter(questionnaireType = questionnaireType)

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int = presenter.size()
}