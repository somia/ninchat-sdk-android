package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.view

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.paris.extensions.style
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.presenter.INinchatRadioButtonPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.presenter.NinchatRadioButtonPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter.OnToggleListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.view.INinchatRadioButtonListView
import kotlinx.android.synthetic.main.radio_item.view.*
import org.json.JSONObject

class NinchatRadioButtonView(
        itemView: View,
        jsonObject: JSONObject?,
        enabled: Boolean,
        private val optionToggleCallback: INinchatRadioButtonListView,
) : RecyclerView.ViewHolder(itemView), INinchatRadioButtonPresenter {

    private val presenter = NinchatRadioButtonPresenter(
            jsonObject = jsonObject,
            enabled = enabled,
            viewCallback = this)

    init {
        presenter.renderCurrentView(enabled = enabled)
        attachUserActionHandler()
    }

    fun update(isSelected: Boolean, enabled: Boolean) {
        presenter.updateCurrentView(isSelected = isSelected, enabled = enabled)
    }

    private fun attachUserActionHandler() {
        itemView.single_radio_item.setOnClickListener {
            presenter.onToggleSelection()
        }
    }

    override fun renderView(label: String, isSelected: Boolean, enabled: Boolean) {
        itemView.single_radio_item.text = label
        itemView.isEnabled = enabled
        // render initialize view
        if (isSelected) {
            itemView.single_radio_item.style(R.style.NinchatTheme_Questionnaire_Radio_Selected)
        } else {
            itemView.single_radio_item.style(R.style.NinchatTheme_Questionnaire_Radio)
        }
    }

    override fun updateView(label: String, isSelected: Boolean, enabled: Boolean) {
        itemView.isEnabled = enabled
        // update view
        if (isSelected) {
            itemView.single_radio_item.style(R.style.NinchatTheme_Questionnaire_Radio_Selected)
        } else {
            itemView.single_radio_item.style(R.style.NinchatTheme_Questionnaire_Radio)
        }
    }

    override fun onSelected() {
        optionToggleCallback.onOptionToggled(isSelected = true, listPosition = layoutPosition)
    }

    override fun onUnSelected() {
        optionToggleCallback.onOptionToggled(isSelected = false, listPosition = layoutPosition)
    }

}

interface INinchatRadioButtonView {
    fun onToggleSelection()
}