package com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.view

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.paris.extensions.style
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.presenter.INinchatInputFieldViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.presenter.InputFieldUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.presenter.NinchatInputFieldViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.presenter.OnChangeListener
import com.ninchat.sdk.utils.misc.Misc
import kotlinx.android.synthetic.main.text_area_with_label.view.*
import kotlinx.android.synthetic.main.text_field_with_label.view.*
import org.json.JSONObject

class NinchatInputFieldViewHolder(
    itemView: View,
    jsonObject: JSONObject?,
    isMultiline: Boolean,
    isFormLikeQuestionnaire: Boolean = true,
    updateCallback: InputFieldUpdateListener,
    position: Int,
    enabled: Boolean,
) : RecyclerView.ViewHolder(itemView), INinchatInputFieldViewPresenter {

    val presenter = NinchatInputFieldViewPresenter(
        jsonObject = jsonObject,
        isMultiline = isMultiline,
        isFormLikeQuestionnaire = isFormLikeQuestionnaire,
        viewCallback = this,
        updateCallback = updateCallback,
        position = position,
        enabled = enabled
    )

    init {
        presenter.renderCurrentView(jsonObject = jsonObject, enabled = enabled)
        attachUserActionHandler()
    }

    val onChangeListener = OnChangeListener(intervalInMs = 100)

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        presenter.updateCurrentView(jsonObject = jsonObject, enabled = enabled)
    }

    private fun attachUserActionHandler() {
        val mEditText =
            if (presenter.isMultiline()) itemView.multiline_text_area else itemView.simple_text_field
        mEditText?.let {
            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(text: Editable?) {
                    onChangeListener.onChange { presenter.onTextChange(text?.toString()) }
                }
            })
            it.onFocusChangeListener = OnFocusChangeListener { v: View?, hasFocus: Boolean ->
                presenter.onFocusChange(hasFocus = hasFocus)
            }
        }

    }

    override fun onRenderFromView(label: String, enabled: Boolean) {
        renderCommonView(isMultiline = presenter.isMultiline(), label = label, enabled = enabled)
    }

    override fun onRenderConversationView(label: String, enabled: Boolean) {
        renderCommonView(isMultiline = presenter.isMultiline(), label = label, enabled = enabled)
    }

    override fun onUpdateFromView(label: String, enabled: Boolean) {
        updateCommonView(isMultiline = presenter.isMultiline(), label = label, enabled = enabled)
    }

    override fun onUpdateConversationView(label: String, enabled: Boolean) {
        updateCommonView(isMultiline = presenter.isMultiline(), label = label, enabled = enabled)
    }

    override fun onUpdateText(value: String, hasError: Boolean) {
        val view =
            if (presenter.isMultiline()) itemView.multiline_text_field_container else itemView.simple_text_field_container
        view.style(
            if (hasError) R.style.NinchatTheme_Questionnaire_InputText_Error else R.style.NinchatTheme_Questionnaire_InputText_Focus
        )
    }

    override fun onUpdateFocus(hasFocus: Boolean) {
        val view =
            if (presenter.isMultiline()) itemView.multiline_text_field_container else itemView.simple_text_field_container
        view.style(
            if (hasFocus) R.style.NinchatTheme_Questionnaire_InputText_Focus else R.style.NinchatTheme_Questionnaire_InputText
        )
    }

    private fun renderCommonView(isMultiline: Boolean, label: String, enabled: Boolean) {
        itemView.isEnabled = enabled
        // set label
        val mLabel = if (isMultiline) itemView.multiline_text_label else itemView.simple_text_label
        mLabel?.let {
            if (label.isNullOrEmpty()) mLabel.visibility = View.GONE
            if (label.isNotBlank()) {
                mLabel.text = Misc.toRichText(label, mLabel)
            }
            mLabel.setTextAppearance(if (enabled) R.style.NinchatTheme_Questionnaire_Label else R.style.NinchatTheme_Questionnaire_Label_Disabled)
        }
        // set input type if it is a simple view
        if (!isMultiline) {
            setInputType()
        }
        val mEditText =
            if (presenter.isMultiline()) itemView.multiline_text_area else itemView.simple_text_field
        mEditText?.let {
            it.text.clear()
            if (presenter.getInputValue().isNullOrBlank().not()) {
                it.setText(presenter.getInputValue())
            }
            it.isEnabled = enabled
            // it.setTextAppearance(if (enabled) R.style.NinchatTheme_Questionnaire_InputText_Focus else R.style.NinchatTheme_Questionnaire_InputText)
            val view =
                if (presenter.isMultiline()) itemView.multiline_text_field_container else itemView.simple_text_field_container
            view.style(
                if (enabled) R.style.NinchatTheme_Questionnaire_InputText_Focus else R.style.NinchatTheme_Questionnaire_InputText
            )
        }
    }

    private fun updateCommonView(isMultiline: Boolean, label: String, enabled: Boolean) {
        itemView.isEnabled = enabled
        // set label
        val mLabel = if (isMultiline) itemView.multiline_text_label else itemView.simple_text_label
        mLabel?.let {
            if (label.isNullOrEmpty()) mLabel.visibility = View.GONE
            if (label.isNotBlank()) {
                mLabel.text = Misc.toRichText(label, mLabel)
            }
            mLabel.setTextAppearance(if (enabled) R.style.NinchatTheme_Questionnaire_Label else R.style.NinchatTheme_Questionnaire_Label_Disabled)
        }
        val mEditText =
            if (presenter.isMultiline()) itemView.multiline_text_area else itemView.simple_text_field
        mEditText?.let {
            it.text.clear()
            if (presenter.getInputValue().isNullOrBlank().not()) {
                it.setText(presenter.getInputValue())
                it.setSelection(presenter.getInputValue()?.length ?: 0)
            }
            it.isEnabled = enabled
            val view =
                if (presenter.isMultiline()) itemView.multiline_text_field_container else itemView.simple_text_field_container
            view.style(
                if (enabled) R.style.NinchatTheme_Questionnaire_InputText_Focus else R.style.NinchatTheme_Questionnaire_InputText
            )
        }
    }

    private fun setInputType() {
        itemView.simple_text_field.inputType = presenter.getInputType()
    }
}


interface INinchatInputFieldViewHolder {
    fun onTextChange(text: String?)
    fun onFocusChange(hasFocus: Boolean)
}