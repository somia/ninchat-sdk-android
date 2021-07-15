package com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.view

import android.graphics.Typeface
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.paris.extensions.style
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
    position: Int,
    checkboxToggleListener: CheckboxUpdateListener,
    enabled: Boolean,
) : RecyclerView.ViewHolder(itemView), INinchatCheckboxViewPresenter {

    private val presenter = NinchatCheckboxViewPresenter(
        jsonObject = jsonObject,
        presenter = this,
        position = position,
        checkboxToggleListener = checkboxToggleListener,
        enabled = enabled
    )

    init {
        presenter.renderView()
        attachHandler()
    }

    private fun attachHandler() {
        itemView.setOnClickListener {
            presenter.onCheckBoxToggled()
        }
    }

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        presenter.updateView(jsonObject = jsonObject, enabled = enabled)
    }

    override fun onRenderView(
        label: String?,
        isChecked: Boolean,
        hasError: Boolean,
        enabled: Boolean
    ) {
        itemView.ninchat_checkbox_label.text = Misc.toRichText(label, itemView.text_view_content)
        onUpdateView(isChecked = isChecked, hasError = hasError, enabled = enabled)
    }

    override fun onUpdateView(isChecked: Boolean, hasError: Boolean, enabled: Boolean) {
        val value =
            itemView.resources.getString(R.string.ninchat_questionnaire_checkbox_label_text_style)
        itemView.isEnabled = enabled
        itemView.ninchat_checkbox_label.isEnabled = enabled
        itemView.ninchat_checkbox_label.setTextAppearance(
            when {
                isChecked -> R.style.NinchatTheme_Questionnaire_Checkbox_Label_Selected
                enabled -> R.style.NinchatTheme_Questionnaire_Checkbox_Label_Focused
                else ->
                    R.style.NinchatTheme_Questionnaire_Checkbox_Label
            }
        )
        itemView.ninchat_checkbox_label.setTypeface(
            itemView.ninchat_checkbox_label.typeface, when {
                value.equals("bold", ignoreCase = true) -> Typeface.BOLD
                else -> Typeface.NORMAL
            }
        )
        itemView.ninchat_checkbox_image.isEnabled = enabled
        itemView.ninchat_checkbox_image.style(
            if (isChecked) R.style.NinchatTheme_Questionnaire_Checkbox_Image_Selected else R.style.NinchatTheme_Questionnaire_Checkbox_Image
        )
    }

    override fun onToggleView(isChecked: Boolean, hasError: Boolean, enabled: Boolean) {
        val value =
            itemView.resources.getString(R.string.ninchat_questionnaire_checkbox_label_text_style)
        itemView.ninchat_checkbox_label.setTextAppearance(
            when {
                isChecked -> R.style.NinchatTheme_Questionnaire_Checkbox_Label_Selected
                enabled -> R.style.NinchatTheme_Questionnaire_Checkbox_Label_Focused
                else ->
                    R.style.NinchatTheme_Questionnaire_Checkbox_Label
            }
        )
        itemView.ninchat_checkbox_label.setTypeface(
            itemView.ninchat_checkbox_label.typeface, when {
                value.equals("bold", ignoreCase = true) -> Typeface.BOLD
                else -> Typeface.NORMAL
            }
        )
        itemView.ninchat_checkbox_image.isEnabled = enabled
        itemView.ninchat_checkbox_image.style(
            if (isChecked) R.style.NinchatTheme_Questionnaire_Checkbox_Image_Selected else R.style.NinchatTheme_Questionnaire_Checkbox_Image
        )
    }


}