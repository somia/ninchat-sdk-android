package com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.presenter.NinchatBotWritingViewPresenter
import org.json.JSONObject

class NinchatBotWriting(
        itemView: View,
        jsonObject: JSONObject?,
        position: Int,
        enabled: Boolean,
) : RecyclerView.ViewHolder(itemView) {
    private val presenter = NinchatBotWritingViewPresenter(
            jsonObject = jsonObject,
            position = position,
            enabled = enabled
    )

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        presenter.updateModel(jsonObject = jsonObject, enabled = enabled)
        presenter.renderCurrentView()

        attachUserActionHandler()
    }

    private fun attachUserActionHandler() {
        // update background of the button

    }

}