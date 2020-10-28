package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.presenter

import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.view.INinchatRadioButtonView
import org.json.JSONObject

class NinchatRadioButtonPresenter(
        jsonObject: JSONObject?,
        val viewCallback: INinchatRadioButtonPresenter,
): INinchatRadioButtonView {

    fun renderCurrentView(jsonObject: JSONObject? = null) {
        // if nothing is selected then donot call
        // onSelected, or onUnSelected

    }

    override fun onToggleSelection() {
        TODO("Not yet implemented")
    }
}

interface INinchatRadioButtonPresenter {
    fun renderView(label: String)
    fun onSelected()
    fun onUnSelected()
}