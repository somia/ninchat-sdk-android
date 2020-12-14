package com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.view

import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.presenter.DropDownSelectUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.presenter.INinchatDropDownSelectViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.presenter.NinchatDropDownSelectViewPresenter
import kotlinx.android.synthetic.main.dropdown_list.view.*
import kotlinx.android.synthetic.main.dropdown_with_label.view.*
import org.json.JSONObject


class NinchatDropDownSelectViewHolder(
        itemView: View,
        jsonObject: JSONObject?,
        isFormLikeQuestionnaire: Boolean = true,
        updateCallback: DropDownSelectUpdateListener,
        position: Int
) : RecyclerView.ViewHolder(itemView), INinchatDropDownSelectViewPresenter {

    val iPresenter = NinchatDropDownSelectViewPresenter(
            jsonObject = jsonObject,
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
            viewCallback = this,
            updateCallback = updateCallback,
            position = position,
    )

    fun update(jsonObject: JSONObject?) {
        iPresenter.renderCurrentView(jsonObject)
        attachUserActionHandler()
    }

    private fun attachUserActionHandler() {
        itemView.ninchat_dropdown_list.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, mView: View?, position: Int, id: Long) {
                iPresenter.onItemSelectionChange(position = position)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    override fun onUpdateFromView(label: String, options: List<String>) {
        renderCommonView(label = label, options = options)
        itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_chat_questionnaire_background)
    }

    override fun onUpdateConversationView(label: String, options: List<String>) {
        renderCommonView(label = label, options = options)
    }

    override fun onSelected(position: Int, hasError: Boolean) {
        itemView.ninchat_dropdown_list?.setSelection(position)
        itemView.dropdown_select_layout.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_dropdown_border_select)
        itemView.ninchat_dropdown_list_icon?.setColorFilter(ContextCompat.getColor(itemView.context, R.color.ninchat_color_dropdown_selected_text))
        itemView.ninchat_dropdown_list.selectedView?.let {
            val currentView = it as TextView
            currentView.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_color_dropdown_selected_text))
        }
        if (hasError) {
            renderErrorView(position)
        }
    }

    override fun onUnSelected(position: Int, hasError: Boolean) {
        itemView.dropdown_select_layout.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_dropdown_border_not_selected)
        itemView.ninchat_dropdown_list_icon?.setColorFilter(ContextCompat.getColor(itemView.context, R.color.ninchat_color_dropdown_unselected_text))
        itemView.ninchat_dropdown_list.selectedView?.let {
            val currentView = it as TextView
            currentView.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_color_dropdown_unselected_text))
        }

        if (hasError) {
            renderErrorView(position)
        }
    }

    private fun renderCommonView(label: String?, options: List<String>) {
        itemView.dropdown_text_label.text = label
        // render adapter view
        val dataAdapter = ArrayAdapter<String>(itemView.context, R.layout.dropdown_item_text_view).apply {
            addAll(options)
        }
        itemView.ninchat_dropdown_list.adapter = dataAdapter
    }

    private fun renderErrorView(position: Int = 0) {
        itemView.dropdown_select_layout.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_dropdown_border_with_error)
        itemView.ninchat_dropdown_list_icon?.setColorFilter(ContextCompat.getColor(itemView.context, R.color.ninchat_color_error_background))
        itemView.ninchat_dropdown_list.selectedView?.let {
            val currentView = it as TextView
            currentView.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_color_error_background))
        }

    }

}

interface INinchatDropDownSelectViewHolder {
    fun onItemSelectionChange(position: Int)
}