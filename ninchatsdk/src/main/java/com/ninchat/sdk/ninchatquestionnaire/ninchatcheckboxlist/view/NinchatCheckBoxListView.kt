package com.ninchat.sdk.ninchatquestionnaire.ninchatcheckboxlist.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.helper.NinchatQuestionnaireItemDecoration
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.presenter.CheckboxUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.view.NinchatCheckboxViewHolder
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckboxlist.presenter.CheckboxListUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckboxlist.presenter.INinchatCheckboxListPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckboxlist.presenter.NinchatCheckBoxListPresenter
import kotlinx.android.synthetic.main.checkbox_compound.view.*
import org.json.JSONObject

class NinchatCheckBoxListView(
        itemView: View,
        jsonObject: JSONObject?,
        isFormLikeQuestionnaire: Boolean,
        position: Int,
        enabled: Boolean,
        updateCallback: CheckboxListUpdateListener
) : RecyclerView.ViewHolder(itemView), INinchatCheckboxListPresenter {

    private val presenter = NinchatCheckBoxListPresenter(
            jsonObject = jsonObject,
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
            position = position,
            enabled = enabled,
            updateCallback = updateCallback,
            viewCallback = this
    )

    init {
        presenter.renderCurrentView(jsonObject = jsonObject, enabled = enabled)
    }

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        presenter.updateCurrentView(jsonObject = jsonObject, enabled = enabled)
    }

    override fun onRenderView(optionsList: List<JSONObject>, enabled: Boolean, isFormView: Boolean) {
        itemView.isEnabled = enabled
        itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_chat_questionnaire_background)
        val spaceInPixel = if (presenter.size() < 2) 0 else itemView.resources.getDimensionPixelSize(R.dimen.ninchat_questionnaire_checkbox_margin)
        itemView.ninchat_chat_checkbox_options?.apply {
            layoutManager = LinearLayoutManager(itemView.context)
            adapter = NinchatCheckboxListViewAdapter()
            addItemDecoration(NinchatQuestionnaireItemDecoration(spaceTop = spaceInPixel, 0, 0, spaceBottom = spaceInPixel))
        }
    }

    override fun onUpdateView(optionsList: List<JSONObject>, enabled: Boolean, isFormView: Boolean) {
        itemView.isEnabled = enabled
    }

    inner class NinchatCheckboxListViewAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {
            val jsonObject = presenter.get(position = position)
            return NinchatCheckboxViewHolder(
                    itemView = LayoutInflater.from(parent.context).inflate(R.layout.checkbox_simple, parent, false),
                    jsonObject = jsonObject,
                    position = position,
                    checkboxToggleListener = presenter,
                    enabled = presenter.isEnabled()
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val jsonObject = presenter.get(position = position)
            (holder as NinchatCheckboxViewHolder).update(jsonObject = jsonObject)
        }

        override fun getItemCount(): Int {
            return presenter.size()
        }
    }
}