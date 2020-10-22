package com.ninchat.sdk.ninchatquestionnaire.view

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.presenter.INinchatInputFieldViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.presenter.NinchatInputFieldViewPresenter
import kotlinx.android.synthetic.main.text_area.view.*
import kotlinx.android.synthetic.main.text_area_with_label.view.*
import kotlinx.android.synthetic.main.text_field.view.*
import kotlinx.android.synthetic.main.text_field_with_label.view.*
import org.json.JSONObject

class NinchatInputFieldViewHolder(
        itemView: View,
        isMultiline: Boolean,
        jsonObject: JSONObject?,
        isFormLikeQuestionnaire: Boolean = true,
) : RecyclerView.ViewHolder(itemView), INinchatInputFieldViewPresenter {

    val iPresenter = NinchatInputFieldViewPresenter(
            jsonObject = jsonObject,
            isMultiline = isMultiline,
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
            viewCallback = this
    )

    fun update(jsonObject: JSONObject?, isFormLikeQuestionnaire: Boolean = true) {
        iPresenter.renderCurrentView()
        attachUserActionHandler()
    }

    private fun attachUserActionHandler() {
        val mEditText = if (iPresenter.isMultiline()) itemView.multiline_text_area else itemView.simple_text_field
        mEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(text: Editable?) {
                iPresenter.onTextChange(text?.toString())
            }
        })
        mEditText.onFocusChangeListener = OnFocusChangeListener { v: View?, hasFocus: Boolean ->
            iPresenter.onFocusChange(hasFocus = hasFocus)
        }
    }

    override fun onUpdateFromView(label: String) {
        val background = R.drawable.ninchat_chat_questionnaire_background
        itemView.background = ContextCompat.getDrawable(itemView.context, background)
        renderCommonView(isMultiline = iPresenter.isMultiline(), label = label)
    }

    override fun onUpdateConversationView(label: String) {
        renderCommonView(isMultiline = iPresenter.isMultiline(), label = label)
    }

    override fun onUpdateText(value: String, hasError: Boolean) {
        val mEditText = if (iPresenter.isMultiline()) itemView.multiline_text_area else itemView.simple_text_field
        mEditText?.setText(value)
        if (hasError) {
            mEditText?.setBackgroundResource(R.drawable.ninchat_border_with_error);
        }
    }

    override fun onUpdateFocus(hasFocus: Boolean) {
        val mEditText = if (iPresenter.isMultiline()) itemView.multiline_text_area else itemView.simple_text_field
        mEditText?.setBackgroundResource(if (hasFocus) R.drawable.ninchat_border_with_focus else R.drawable.ninchat_border_with_unfocus)
    }

    private fun renderCommonView(isMultiline: Boolean, label: String) {
        // set label
        val mLabel = if (isMultiline) itemView.multiline_text_label else itemView.simple_text_label
        mLabel.text = label

        // set input type if it is a simple view
        if (!isMultiline) {
            setInputType()
        }
    }

    private fun setInputType() {
        itemView.simple_text_label.inputType = iPresenter.getInputType()
    }
}


interface INinchatInputFieldViewHolder {
    fun onTextChange(text: String?)
    fun onFocusChange(hasFocus: Boolean)
}