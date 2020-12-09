package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.view

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.presenter.INinchatRadioButtonPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.presenter.NinchatRadioButtonPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.view.INinchatRadioButtonListView
import kotlinx.android.synthetic.main.radio_item.view.*
import org.json.JSONObject

class NinchatRadioButtonView(
        itemView: View,
        jsonObject: JSONObject?,
        private val optionToggleCallback: INinchatRadioButtonListView,
) : RecyclerView.ViewHolder(itemView), INinchatRadioButtonPresenter {

    private val ninchatRadioButtonPresenter = NinchatRadioButtonPresenter(
            jsonObject = jsonObject,
            viewCallback = this)

    fun update(isSelected: Boolean) {
        ninchatRadioButtonPresenter.renderCurrentView(isSelected = isSelected)
        attachUserActionHandler()
    }

    private fun attachUserActionHandler() {
        itemView.single_radio_item.setOnClickListener { v: View ->
            ninchatRadioButtonPresenter.onToggleSelection()
        }
    }

    override fun renderView(label: String, isSelected: Boolean) {
        itemView.single_radio_item.text = label
        // render initialize view
        if (isSelected) {
            itemView.single_radio_item.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_color_radio_item_selected_text))
            itemView.single_radio_item.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_radio_select_button)
        } else {
            itemView.single_radio_item.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_color_radio_item_unselected_text))
            itemView.single_radio_item.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_ui_compose_select_button)
        }
    }

    override fun onSelected() {
        itemView.single_radio_item.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_color_radio_item_selected_text))
        itemView.single_radio_item.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_radio_select_button)
        optionToggleCallback.onOptionToggled(isSelected = true, position = layoutPosition)
    }

    override fun onUnSelected() {
        itemView.single_radio_item.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_color_radio_item_unselected_text))
        itemView.single_radio_item.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_ui_compose_select_button)
        optionToggleCallback.onOptionToggled(isSelected = false, position = layoutPosition)
    }

}

interface INinchatRadioButtonView {
    fun onToggleSelection()
}