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

    fun renderCurrentView(isSelected: Boolean, hasError: Boolean) {
        ninchatRadioButtonModel.update(
                isSelected = isSelected,
                hasError = hasError
        )

        // if nothing is selected then do not call
        viewCallback.renderView(
                label = ninchatRadioButtonModel.label ?: "",
                isSelected = ninchatRadioButtonModel.isSelected,
                hasError = ninchatRadioButtonModel.hasError
        )
    }

    override fun onToggleSelection() {
        ninchatRadioButtonModel.isSelected = !ninchatRadioButtonModel.isSelected
        if (ninchatRadioButtonModel.isSelected) {
            viewCallback.onSelected()
        } else {
            viewCallback.onUnSelected()
        }
        if (ninchatRadioButtonModel.hasError) {
            viewCallback.onError()
        }
    }

    fun getLabel() = ninchatRadioButtonModel.label
    internal fun getNinchatRadioButtonModel(): NinchatRadioButtonModel = ninchatRadioButtonModel
}

interface INinchatRadioButtonPresenter {
    fun renderView(label: String, isSelected: Boolean, hasError: Boolean)
    fun onSelected()
    fun onUnSelected()
    fun onError()
}