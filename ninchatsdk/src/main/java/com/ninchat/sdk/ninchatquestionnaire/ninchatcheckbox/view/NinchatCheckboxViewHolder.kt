package com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.view

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.presenter.CheckboxUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.presenter.INinchatCheckboxViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.presenter.NinchatCheckboxViewPresenter
import com.ninchat.sdk.utils.misc.Misc
import kotlinx.android.synthetic.main.checkbox_simple.view.*
import kotlinx.android.synthetic.main.text_view.view.*
import org.json.JSONObject

class NinchatCheckboxViewHolder(
        itemView: View,
        jsonObject: JSONObject?,
        isFormLikeQuestionnaire: Boolean = true,
        updateCallback: CheckboxUpdateListener,
        position: Int,
        enabled: Boolean,
) : RecyclerView.ViewHolder(itemView), INinchatCheckboxViewPresenter {

    private val presenter = NinchatCheckboxViewPresenter(
            jsonObject = jsonObject,
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
            iPresent = this,
            updateCallback = updateCallback,
            position = position,
            enabled = enabled
    )

    init {
        presenter.renderCurrentView(jsonObject = jsonObject, enabled = enabled)
        attachUserActionHandler()
    }

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        presenter.updateCurrentView(jsonObject = jsonObject, enabled = enabled)
    }

    private fun attachUserActionHandler() {
        itemView.run {
            setOnClickListener { _ ->
                presenter.handleCheckBoxToggled()
            }
        }
    }

    override fun onRenderFromView(label: String?, isChecked: Boolean, hasError: Boolean, enabled: Boolean) {
        renderCommonView(label = label, isChecked = isChecked, hasError = hasError, enabled = enabled)
    }

    override fun onRenderConversationView(label: String?, isChecked: Boolean, hasError: Boolean, enabled: Boolean) {
        renderCommonView(label = label, isChecked = isChecked, hasError = hasError, enabled = enabled)
    }


    override fun onUpdateFromView(label: String?, isChecked: Boolean, hasError: Boolean, enabled: Boolean) {
        updateCommonView(label = label, isChecked = isChecked, hasError = hasError, enabled = enabled)
    }

    override fun onUpdateConversationView(label: String?, isChecked: Boolean, hasError: Boolean, enabled: Boolean) {
        updateCommonView(label = label, isChecked = isChecked, hasError = hasError, enabled = enabled)
    }

    override fun onCheckBoxToggled(isChecked: Boolean, hasError: Boolean) {
        itemView.ninchat_checkbox_label.setTextColor(ContextCompat.getColor(itemView.context,
                if (isChecked) R.color.ninchat_color_checkbox_selected else R.color.ninchat_color_checkbox_unselected));
        itemView.ninchat_checkbox_image.setImageDrawable(if (isChecked) ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_chat_checkbox_selected) else ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_chat_checkbox_unselected))
    }

    private fun renderCommonView(label: String?, isChecked: Boolean, hasError: Boolean, enabled: Boolean) {
        itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_chat_questionnaire_background)
        itemView.isEnabled = enabled
        itemView.ninchat_checkbox_label.isEnabled = enabled
        itemView.ninchat_checkbox_label.text = Misc.toRichText(label, itemView.text_view_content)
        itemView.ninchat_checkbox_label.setTextColor(ContextCompat.getColor(itemView.context,
                when {
                    hasError -> R.color.ninchat_color_error_background
                    isChecked -> R.color.ninchat_color_checkbox_selected
                    enabled -> R.color.ninchat_color_text_disabled
                    else ->
                        R.color.ninchat_color_checkbox_unselected
                }))

        itemView.ninchat_checkbox_image.isEnabled = enabled
        itemView.ninchat_checkbox_image.setImageDrawable(if (isChecked) ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_chat_checkbox_selected) else ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_chat_checkbox_unselected))

        if (hasError) {
            itemView.ninchat_checkbox_label.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_color_error_background));
        }
    }

    private fun updateCommonView(label: String?, isChecked: Boolean, hasError: Boolean, enabled: Boolean) {
        itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_chat_questionnaire_background)
        itemView.isEnabled = enabled
        itemView.ninchat_checkbox_label.isEnabled = enabled
        itemView.ninchat_checkbox_label.setTextColor(ContextCompat.getColor(itemView.context,
                when {
                    hasError -> R.color.ninchat_color_error_background
                    isChecked -> R.color.ninchat_color_checkbox_selected
                    enabled -> R.color.ninchat_color_text_disabled
                    else ->
                        R.color.ninchat_color_checkbox_unselected
                }))

        itemView.ninchat_checkbox_image.isEnabled = enabled
        itemView.ninchat_checkbox_image.setImageDrawable(if (isChecked) ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_chat_checkbox_selected) else ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_chat_checkbox_unselected))
        if (hasError) {
            itemView.ninchat_checkbox_label.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_color_error_background));
        }
    }

}