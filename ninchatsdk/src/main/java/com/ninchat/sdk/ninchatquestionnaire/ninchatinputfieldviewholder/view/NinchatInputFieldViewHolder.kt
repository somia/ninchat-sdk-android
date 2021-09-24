package com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.view

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.paris.extensions.style
import com.ninchat.sdk.R
import com.ninchat.sdk.events.OnItemFocus
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.presenter.INinchatInputFieldViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.presenter.InputFieldUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.presenter.NinchatInputFieldViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.presenter.OnChangeListener
import com.ninchat.sdk.utils.misc.Misc
import kotlinx.android.synthetic.main.text_area_with_label.view.*
import kotlinx.android.synthetic.main.text_field_with_label.view.*
import org.greenrobot.eventbus.EventBus
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
            it.onFocusChangeListener = OnFocusChangeListener { _, hasFocus: Boolean ->
                presenter.onFocusChange(hasFocus = hasFocus)
            }
            it.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    EventBus.getDefault().post(OnItemFocus(presenter.position(), true))
                    it.clearFocus()
                    return@OnEditorActionListener true
                }
                false
            })
        }

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
        if (hasFocus)
            EventBus.getDefault().post(OnItemFocus(presenter.position(), false))
    }

    override fun renderCommonView(
        isMultiline: Boolean,
        label: String,
        enabled: Boolean,
        isFormLike: Boolean
    ) {
        itemView.isEnabled = enabled
        // set label
        val mLabel = if (isMultiline) itemView.multiline_text_label else itemView.simple_text_label
        mLabel?.let {
            if (label.isNullOrEmpty()) mLabel.visibility = View.GONE
            if (label.isNotBlank()) {
                mLabel.text = Misc.toRichText(label, mLabel)
            }
            mLabel.style(
                if (enabled) if (isFormLike) R.style.NinchatTheme_Questionnaire_Label_Form else R.style.NinchatTheme_Questionnaire_Label
                else if (isFormLike) R.style.NinchatTheme_Questionnaire_Label_Form_Disabled else R.style.NinchatTheme_Questionnaire_Label_Disabled
            )
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

    override fun updateCommonView(
        isMultiline: Boolean,
        label: String,
        enabled: Boolean,
        isFormLike: Boolean
    ) {
        itemView.isEnabled = enabled
        // set label
        val mLabel = if (isMultiline) itemView.multiline_text_label else itemView.simple_text_label
        mLabel?.let {
            if (label.isNullOrEmpty()) mLabel.visibility = View.GONE
            if (label.isNotBlank()) {
                mLabel.text = Misc.toRichText(label, mLabel)
            }
            mLabel.style(
                if (enabled) if (isFormLike) R.style.NinchatTheme_Questionnaire_Label_Form else R.style.NinchatTheme_Questionnaire_Label
                else if (isFormLike) R.style.NinchatTheme_Questionnaire_Label_Form_Disabled else R.style.NinchatTheme_Questionnaire_Label_Disabled
            )
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