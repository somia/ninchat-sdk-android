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
        enabled: Boolean,
) : RecyclerView.ViewHolder(itemView), INinchatCheckboxViewPresenter {

    private val presenter = NinchatCheckboxViewPresenter(
            jsonObject = jsonObject,
            presenter = this,
            enabled = enabled
    )

    init {
        presenter.renderView()
    }

    fun update(jsonObject: JSONObject?) {
        presenter.updateView(jsonObject = jsonObject)
    }

    override fun onRenderView(label: String?, isChecked: Boolean, hasError: Boolean, enabled: Boolean) {
        itemView.ninchat_checkbox_label.text = Misc.toRichText(label, itemView.text_view_content)
        onUpdateView(isChecked = isChecked, hasError = hasError, enabled = enabled)
    }

    override fun onUpdateView(isChecked: Boolean, hasError: Boolean, enabled: Boolean) {
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