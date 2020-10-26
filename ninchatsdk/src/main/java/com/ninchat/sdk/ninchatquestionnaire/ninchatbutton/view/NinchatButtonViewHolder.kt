package com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.view

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.presenter.INinchatButtonViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.presenter.NinchatButtonViewPresenter
import kotlinx.android.synthetic.main.control_buttons.view.*
import org.json.JSONObject

class NinchatButtonViewHolder(
        itemView: View,
        jsonObject: JSONObject?,
) : RecyclerView.ViewHolder(itemView), INinchatButtonViewPresenter {
    private val ninchatButtonViewPresenter: NinchatButtonViewPresenter = NinchatButtonViewPresenter(
            jsonObject = jsonObject,
            iPresenter = this)

    fun update(jsonObject: JSONObject?) {
        ninchatButtonViewPresenter.renderCurrentView()
        attachUserActionHandler()
    }

    private fun attachUserActionHandler() {
        // update background of the button
        itemView.run {
            ninchat_image_button_previous?.setOnClickListener {
                ninchatButtonViewPresenter.onBackButtonClicked()
            }
            ninchat_button_previous?.setOnClickListener {
                ninchatButtonViewPresenter.onBackButtonClicked()
            }
            ninchat_image_button_next?.setOnClickListener {
                ninchatButtonViewPresenter.onNextButtonClicked()
            }
            ninchat_button_next?.setOnClickListener {
                ninchatButtonViewPresenter.onNextButtonClicked()
            }
        }
    }

    override fun onBackButtonUpdated(visible: Boolean, text: String?, imageButton: Boolean, clicked: Boolean) {
        val background = if (clicked) R.drawable.ninchat_chat_secondary_onclicked_button else R.drawable.ninchat_chat_secondary_button
        itemView.run {
            if (imageButton) {
                ninchat_image_button_previous.background = ContextCompat.getDrawable(itemView.context, background)
                ninchat_image_button_previous?.visibility = if (visible) View.VISIBLE else View.GONE
            } else {
                ninchat_button_previous?.background = ContextCompat.getDrawable(itemView.context, background)
                ninchat_button_previous?.visibility = if (visible) View.VISIBLE else View.GONE
                ninchat_button_previous?.text = text
            }
        }
    }

    override fun onNextNextUpdated(visible: Boolean, text: String?, imageButton: Boolean, clicked: Boolean) {
        val background = if (clicked) R.drawable.ninchat_chat_primary_oncliked_button else R.drawable.ninchat_chat_primary_button
        itemView.run {
            if (imageButton) {
                ninchat_image_button_next?.background = ContextCompat.getDrawable(itemView.context, background)
                ninchat_image_button_next?.visibility = if (visible) View.VISIBLE else View.GONE
            } else {
                ninchat_button_next?.background = ContextCompat.getDrawable(itemView.context, background)
                ninchat_button_next?.visibility = if (visible) View.VISIBLE else View.GONE
                ninchat_button_next?.text = text
            }
        }
    }
}

interface INinchatButtonViewHolder {
    fun onBackButtonClicked()
    fun onNextButtonClicked()
}