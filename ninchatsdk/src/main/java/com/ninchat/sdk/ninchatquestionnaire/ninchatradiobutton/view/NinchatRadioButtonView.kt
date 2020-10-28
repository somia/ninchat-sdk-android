package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.view

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.presenter.INinchatRadioButtonPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.presenter.NinchatRadioButtonPresenter
import kotlinx.android.synthetic.main.radio_item.view.*
import org.json.JSONObject

class NinchatRadioButtonView(
        itemView: View,
        jsonObject: JSONObject?,
) : RecyclerView.ViewHolder(itemView), INinchatRadioButtonPresenter {

    private val ninchatRadioButtonPresenter = NinchatRadioButtonPresenter(
            jsonObject = jsonObject,
            viewCallback = this
    )

    fun update(jsonObject: JSONObject? = null) {
        ninchatRadioButtonPresenter.renderCurrentView(jsonObject = jsonObject)
        attachUserActionHandler()
    }

    private fun attachUserActionHandler() {
        itemView.single_radio_item.setOnClickListener { v: View ->
            ninchatRadioButtonPresenter.onToggleSelection()
        }
    }

    private fun renderError() {
        itemView.single_radio_item.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_color_error_background))
    }

    override fun renderView(label: String) {
        itemView.single_radio_item.text = label
        itemView.single_radio_item.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_color_radio_item_unselected_text))
        itemView.single_radio_item.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_ui_compose_select_button)
    }

    override fun onSelected() {
        itemView.single_radio_item.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_color_radio_item_selected_text))
        itemView.single_radio_item.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_radio_select_button)
    }

    override fun onUnSelected() {
        itemView.single_radio_item.setTextColor(ContextCompat.getColor(itemView.context, R.color.ninchat_color_radio_item_unselected_text))
        itemView.single_radio_item.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_ui_compose_select_button)
    }
}

interface INinchatRadioButtonView {
    fun onToggleSelection()
}