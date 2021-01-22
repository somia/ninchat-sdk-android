package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.presenter

import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.model.NinchatRadioButtonModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.view.INinchatRadioButtonView
import org.json.JSONObject

class NinchatRadioButtonPresenter(
        jsonObject: JSONObject?,
        enabled: Boolean,
        val viewCallback: INinchatRadioButtonPresenter,
) : INinchatRadioButtonView {

    private val model = NinchatRadioButtonModel(enabled = enabled).apply {
        parse(jsonObject = jsonObject)
    }

    fun renderCurrentView(enabled: Boolean) {
        model.update(isSelected = model.isSelected, enabled = enabled)
        // if nothing is selected then do not call
        viewCallback.renderView(
                label = model.label ?: "",
                isSelected = model.isSelected,
                enabled = model.enabled
        )
    }

    fun updateCurrentView(isSelected: Boolean, enabled: Boolean) {
        model.update(isSelected = isSelected, enabled = enabled)
        // if nothing is selected then do not call
        viewCallback.updateView(
                label = model.label ?: "",
                isSelected = model.isSelected,
                enabled = model.enabled
        )
    }

    override fun onToggleSelection() {
        model.isSelected = !model.isSelected
        if (model.isSelected) {
            viewCallback.onSelected()
        } else {
            viewCallback.onUnSelected()
        }
    }

    fun getLabel() = model.label
    internal fun getNinchatRadioButtonModel(): NinchatRadioButtonModel = model
}

interface INinchatRadioButtonPresenter {
    fun renderView(label: String, isSelected: Boolean, enabled: Boolean)
    fun updateView(label: String, isSelected: Boolean, enabled: Boolean)
    fun onSelected()
    fun onUnSelected()
}