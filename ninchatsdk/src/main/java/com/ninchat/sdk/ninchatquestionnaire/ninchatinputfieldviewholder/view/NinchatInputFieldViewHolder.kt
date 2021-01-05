package com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.view

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.presenter.INinchatInputFieldViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.presenter.InputFieldUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.presenter.NinchatInputFieldViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.presenter.OnChangeListener
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

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        presenter.renderCurrentView(jsonObject = jsonObject, enabled = enabled)
    }

    private fun attachUserActionHandler() {
        val mEditText = if (presenter.isMultiline()) itemView.multiline_text_area else itemView.simple_text_field
        val onChangeListener = OnChangeListener(
                intervalInMs = 100,
                callback = {
                    presenter.onTextChange(it?.toString())
                }
        )

        mEditText?.let {
            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(text: Editable?) {
                    onChangeListener.onChange(text = text)
                }
            })
            it.onFocusChangeListener = OnFocusChangeListener { v: View?, hasFocus: Boolean ->
                presenter.onFocusChange(hasFocus = hasFocus)
            }
        }

    }

    override fun onUpdateFromView(label: String, enabled: Boolean) {
        renderCommonView(isMultiline = presenter.isMultiline(), label = label, enabled = enabled)
    }

    override fun onUpdateConversationView(label: String, enabled: Boolean) {
        renderCommonView(isMultiline = presenter.isMultiline(), label = label, enabled = enabled)
    }

    override fun onUpdateText(value: String, hasError: Boolean) {
        val mEditText = if (presenter.isMultiline()) itemView.multiline_text_area else itemView.simple_text_field
        if (hasError) {
            mEditText?.setBackgroundResource(R.drawable.ninchat_border_with_error);
        } else {
            mEditText?.setBackgroundResource(R.drawable.ninchat_border_with_focus);
        }
    }

    override fun onUpdateFocus(hasFocus: Boolean) {
        val mEditText = if (presenter.isMultiline()) itemView.multiline_text_area else itemView.simple_text_field
        mEditText?.setBackgroundResource(if (hasFocus) R.drawable.ninchat_border_with_focus else R.drawable.ninchat_border_with_unfocus)
    }

    private fun renderCommonView(isMultiline: Boolean, label: String, enabled: Boolean) {
        itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_chat_questionnaire_background)
        itemView.isEnabled = enabled
        // set label
        val mLabel = if (isMultiline) itemView.multiline_text_label else itemView.simple_text_label
        // set color of the label
        val textColor = if (enabled) R.color.ninchat_color_text_normal else R.color.ninchat_color_text_disabled
        mLabel?.let {
            if (label.isNotBlank())
                mLabel.text = label
            mLabel.setTextColor(ContextCompat.getColor(itemView.context, textColor))
        }
        // set input type if it is a simple view
        if (!isMultiline) {
            setInputType()
        }

        val mEditText = if (presenter.isMultiline()) itemView.multiline_text_area else itemView.simple_text_field
        mEditText?.let {
        //    it.text.clear()
            if (presenter.getInputValue().isNullOrBlank().not()) {
                it.setText(presenter.getInputValue())
            }
            it.isEnabled = enabled
            it.setTextColor(ContextCompat.getColor(itemView.context, textColor))
        }
        attachUserActionHandler()
    }

    private fun setInputType() {
        itemView.simple_text_field.inputType = presenter.getInputType()
    }
}


interface INinchatInputFieldViewHolder {
    fun onTextChange(text: String?)
    fun onFocusChange(hasFocus: Boolean)
}