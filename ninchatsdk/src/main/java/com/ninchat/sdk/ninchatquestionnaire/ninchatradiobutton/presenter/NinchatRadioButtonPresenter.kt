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
                enabled = model.enabled,
                isHrefElement = model.hasHrefAttribute(),
                hasLabel = model.hasLabel,
        )
    }

    fun updateCurrentView(isSelected: Boolean, enabled: Boolean) {
        model.update(isSelected = isSelected, enabled = enabled)
        // if nothing is selected then do not call
        viewCallback.updateView(
                label = model.label ?: "",
                isSelected = model.isSelected,
                enabled = model.enabled,
                isHrefElement = model.hasHrefAttribute(),
                hasLabel = model.hasLabel,
        )
    }

    override fun onToggleSelection() {
        model.isSelected = !model.isSelected
        if (model.isSelected) {
            viewCallback.onSelected(isHrefElement = model.hasHrefAttribute(), hasLabel = model.hasLabel, uri = model.hrefAttribute)
        } else {
            viewCallback.onUnSelected(isHrefElement = model.hasHrefAttribute(), hasLabel = model.hasLabel)
        }
    }

    fun getLabel() = model.label
    internal fun getNinchatRadioButtonModel(): NinchatRadioButtonModel = model
}

interface INinchatRadioButtonPresenter {
    fun renderView(label: String, isSelected: Boolean, enabled: Boolean, isHrefElement: Boolean, hasLabel: Boolean)
    fun updateView(label: String, isSelected: Boolean, enabled: Boolean, isHrefElement: Boolean, hasLabel: Boolean)
    fun onSelected(isHrefElement: Boolean, hasLabel: Boolean, uri: String)
    fun onUnSelected(isHrefElement: Boolean, hasLabel: Boolean)
}