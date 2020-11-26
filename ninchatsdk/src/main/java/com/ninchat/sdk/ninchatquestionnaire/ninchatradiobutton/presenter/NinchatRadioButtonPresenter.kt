package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.presenter

import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.model.NinchatRadioButtonModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.view.INinchatRadioButtonView
import org.json.JSONObject

class NinchatRadioButtonPresenter(
        jsonObject: JSONObject?,
        val viewCallback: INinchatRadioButtonPresenter,
) : INinchatRadioButtonView {

    private val ninchatRadioButtonModel = NinchatRadioButtonModel().apply {
        parse(jsonObject = jsonObject)
    }

    fun renderCurrentView(isSelected: Boolean) {
        ninchatRadioButtonModel.update(isSelected = isSelected)
        // if nothing is selected then do not call
        viewCallback.renderView(
                label = ninchatRadioButtonModel.label ?: "",
                isSelected = ninchatRadioButtonModel.isSelected,
        )
    }

    override fun onToggleSelection() {
        ninchatRadioButtonModel.isSelected = !ninchatRadioButtonModel.isSelected
        if (ninchatRadioButtonModel.isSelected) {
            viewCallback.onSelected()
        } else {
            viewCallback.onUnSelected()
        }
    }

    fun getLabel() = ninchatRadioButtonModel.label
    internal fun getNinchatRadioButtonModel(): NinchatRadioButtonModel = ninchatRadioButtonModel
}

interface INinchatRadioButtonPresenter {
    fun renderView(label: String, isSelected: Boolean)
    fun onSelected()
    fun onUnSelected()
}