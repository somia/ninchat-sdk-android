package com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.view

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.presenter.CheckboxUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.presenter.INinchatCheckboxViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.presenter.NinchatCheckboxViewPresenter
import kotlinx.android.synthetic.main.checkbox_simple.view.*
import org.json.JSONObject

class NinchatCheckboxViewHolder(
        itemView: View,
        jsonObject: JSONObject?,
        isFormLikeQuestionnaire: Boolean = true,
        updateCallback: CheckboxUpdateListener,
        position: Int,
        enabled: Boolean,
) : RecyclerView.ViewHolder(itemView), INinchatCheckboxViewPresenter {

    private val ninchatCheckboxViewPresenter = NinchatCheckboxViewPresenter(
            jsonObject = jsonObject,
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
            iPresent = this,
            updateCallback = updateCallback,
            position = position,
            enabled = enabled
    )

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        ninchatCheckboxViewPresenter.renderCurrentView(jsonObject = jsonObject, enabled = enabled)
        attachUserActionHandler()
    }

    private fun attachUserActionHandler() {
        itemView.run {
            ninchat_checkbox.setOnCheckedChangeListener { _, isChecked ->
                ninchatCheckboxViewPresenter.handleCheckBoxToggled(isChecked)
            }
        }
    }


    override fun onUpdateFromView(label: String?, isChecked: Boolean, hasError: Boolean, enabled: Boolean) {
        renderCommonView(label = label, isChecked = isChecked, hasError = hasError, enabled = enabled)
    }

    override fun onUpdateConversationView(label: String?, isChecked: Boolean, hasError: Boolean, enabled: Boolean) {
        renderCommonView(label = label, isChecked = isChecked, hasError = hasError, enabled = enabled)
    }

    override fun onCheckBoxToggled(isChecked: Boolean, hasError: Boolean) {
        itemView.ninchat_checkbox.isChecked = isChecked
        itemView.ninchat_checkbox.setTextColor(ContextCompat.getColor(itemView.context,
                if (isChecked) R.color.ninchat_color_checkbox_selected else R.color.ninchat_color_checkbox_unselected));
    }

    private fun renderCommonView(label: String?, isChecked: Boolean, hasError: Boolean, enabled: Boolean) {
        itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_chat_questionnaire_background)
        itemView.isEnabled = enabled
        itemView.ninchat_checkbox.isEnabled = enabled
        itemView.ninchat_checkbox.text = label
        itemView.ninchat_checkbox.isChecked = isChecked
        itemView.ninchat_checkbox.setTextColor(ContextCompat.getColor(itemView.context,
                when {
                    hasError -> R.color.ninchat_color_error_background
                    isChecked -> R.color.ninchat_color_checkbox_selected
                    enabled -> R.color.ninchat_color_text_disabled
                    else ->
                        R.color.ninchat_color_checkbox_unselected
                }))

        if (hasError) {
            itemView.ninchat_checkbox.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_color_error_background));
        }
    }

}