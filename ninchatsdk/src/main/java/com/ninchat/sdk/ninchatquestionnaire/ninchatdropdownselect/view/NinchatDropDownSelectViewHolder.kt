package com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.view

import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.paris.extensions.style
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.presenter.DropDownSelectUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.presenter.INinchatDropDownSelectViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.presenter.NinchatDropDownSelectViewPresenter
import com.ninchat.sdk.utils.misc.Misc
import kotlinx.android.synthetic.main.dropdown_list.view.*
import kotlinx.android.synthetic.main.dropdown_with_label.view.*
import org.json.JSONObject


class NinchatDropDownSelectViewHolder(
    itemView: View,
    jsonObject: JSONObject?,
    isFormLikeQuestionnaire: Boolean = true,
    updateCallback: DropDownSelectUpdateListener,
    position: Int,
    enabled: Boolean,
) : RecyclerView.ViewHolder(itemView), INinchatDropDownSelectViewPresenter {

    val presenter = NinchatDropDownSelectViewPresenter(
        jsonObject = jsonObject,
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

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        presenter.updateCurrentView(jsonObject, enabled = enabled)
    }

    private fun attachUserActionHandler() {
        itemView.ninchat_dropdown_list.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                mView: View?,
                position: Int,
                id: Long
            ) {
                presenter.onItemSelectionChange(position = position)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        itemView.ninchat_dropdown_list_icon.setOnClickListener {
            if (presenter.isEnabled())
                itemView.ninchat_dropdown_list.performClick()
        }
    }

    override fun onRenderFromView(
        label: String,
        options: List<String>,
        enabled: Boolean,
        hasError: Boolean,
        selectedIndex: Int
    ) {
        renderCommonView(
            label = label,
            options = options,
            enabled = enabled,
            hasError = hasError,
            selectedIndex = selectedIndex
        )
    }

    override fun onRenderConversationView(
        label: String,
        options: List<String>,
        enabled: Boolean,
        hasError: Boolean,
        selectedIndex: Int
    ) {
        renderCommonView(
            label = label,
            options = options,
            enabled = enabled,
            hasError = hasError,
            selectedIndex = selectedIndex
        )
    }

    override fun onUpdateFromView(
        label: String,
        options: List<String>,
        enabled: Boolean,
        hasError: Boolean,
        selectedIndex: Int
    ) {
        updateCommonView(
            label = label,
            options = options,
            enabled = enabled,
            hasError = hasError,
            selectedIndex = selectedIndex
        )
    }

    override fun onUpdateConversationView(
        label: String,
        options: List<String>,
        enabled: Boolean,
        hasError: Boolean,
        selectedIndex: Int
    ) {
        updateCommonView(
            label = label,
            options = options,
            enabled = enabled,
            hasError = hasError,
            selectedIndex = selectedIndex
        )
    }

    override fun onSelectionChange(
        selectedIndex: Int,
        isSelected: Boolean,
        hasError: Boolean,
        enabled: Boolean
    ) {
        itemView.dropdown_select_layout.style(
            if (isSelected) R.style.NinchatTheme_Questionnaire_Dropdown_Selected else R.style.NinchatTheme_Questionnaire_Dropdown
        )
        itemView.ninchat_dropdown_list_icon?.style(
            if (isSelected) R.style.NinchatTheme_Questionnaire_Dropdown_Image_Selected else R.style.NinchatTheme_Questionnaire_Dropdown_Image
        )
        itemView.ninchat_dropdown_list?.apply {
            setSelection(selectedIndex)
            selectedView?.let {
                val currentView = it as TextView
                currentView.setTextAppearance(if (isSelected) R.style.NinchatTheme_Questionnaire_Dropdown_Text_Selected else R.style.NinchatTheme_Questionnaire_Dropdown_Text)
            }
        }
        if (hasError) {
            renderErrorView(selectedIndex)
        }
    }

    private fun renderCommonView(
        label: String?,
        options: List<String>,
        enabled: Boolean,
        hasError: Boolean,
        selectedIndex: Int
    ) {
        itemView.apply {
            isEnabled = enabled
        }
        itemView.dropdown_text_label?.apply {
            isEnabled = enabled
            setTextAppearance(if (enabled) R.style.NinchatTheme_Questionnaire_Label else R.style.NinchatTheme_Questionnaire_Label_Disabled)
            text = Misc.toRichText(label, this)
            if (label.isNullOrEmpty()) visibility = View.GONE
        }
        // render adapter view
        itemView.ninchat_dropdown_list?.apply {
            adapter =
                ArrayAdapter<String>(itemView.context, R.layout.dropdown_item_text_view).apply {
                    addAll(options)
                }
            isEnabled = enabled
            setSelection(selectedIndex)
        }
        if (hasError) {
            renderErrorView()
        }
    }

    private fun updateCommonView(
        label: String?,
        options: List<String>,
        enabled: Boolean,
        hasError: Boolean,
        selectedIndex: Int
    ) {
        itemView.apply {
            isEnabled = enabled
            dropdown_text_label.isEnabled = enabled
            ninchat_dropdown_list.isEnabled = enabled
            dropdown_text_label.setTextAppearance(if (enabled) R.style.NinchatTheme_Questionnaire_Label else R.style.NinchatTheme_Questionnaire_Label_Disabled)
            ninchat_dropdown_list.setSelection(selectedIndex)
            if (label.isNullOrEmpty()) dropdown_text_label.visibility = View.GONE
        }

        if (hasError) {
            renderErrorView()
        }
    }

    private fun renderErrorView(position: Int = 0) {
        itemView.dropdown_select_layout.style(R.style.NinchatTheme_Questionnaire_Dropdown_Error)
        itemView.ninchat_dropdown_list.style(R.style.NinchatTheme_Questionnaire_Dropdown_Spinner)
    }

}

interface INinchatDropDownSelectViewHolder {
    fun onItemSelectionChange(position: Int)
}