package com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.view

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.presenter.INinchatButtonViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.presenter.NinchatButtonViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.presenter.OnClickListener
import kotlinx.android.synthetic.main.control_buttons.view.*
import org.json.JSONObject

class NinchatButtonViewHolder(
        itemView: View,
        jsonObject: JSONObject?,
        position: Int,
        enabled: Boolean,
) : RecyclerView.ViewHolder(itemView), INinchatButtonViewPresenter {
    private val presenter = NinchatButtonViewPresenter(
            jsonObject = jsonObject,
            iPresenter = this,
            position = position,
            enabled = enabled
    )

    init {
        presenter.renderCurrentView()
        attachUserActionHandler()
    }

    private val onClickListener = OnClickListener(intervalInMs = 2000)

    fun update(jsonObject: JSONObject?, enabled: Boolean) {
        presenter.updateModel(jsonObject = jsonObject, enabled = enabled)
        presenter.updateCurrentView()
    }

    private fun attachUserActionHandler() {
        // update background of the button
        itemView.run {
            ninchat_image_button_previous?.setOnClickListener {
                onClickListener.onClickListener { presenter.onBackButtonClicked() }
            }
            ninchat_button_previous?.setOnClickListener {
                onClickListener.onClickListener { presenter.onBackButtonClicked() }
            }
            ninchat_image_button_next?.setOnClickListener {
                onClickListener.onClickListener { presenter.onNextButtonClicked() }
            }
            ninchat_button_next?.setOnClickListener {
                onClickListener.onClickListener { presenter.onNextButtonClicked() }
            }
        }
    }

    override fun onBackButtonUpdated(visible: Boolean, text: String?, imageButton: Boolean, clicked: Boolean, enabled: Boolean) {
        val background = if (enabled) if (clicked) R.drawable.ninchat_chat_secondary_onclicked_button else R.drawable.ninchat_chat_secondary_button else R.drawable.ninchat_chat_disable_button
        itemView.run {
            isEnabled = enabled
            if (imageButton) {
                ninchat_image_button_previous?.isEnabled = enabled
                ninchat_image_button_previous?.background = ContextCompat.getDrawable(itemView.context, background)
                ninchat_image_button_previous?.visibility = if (visible) View.VISIBLE else View.GONE
            } else {
                ninchat_button_previous?.isEnabled = enabled
                ninchat_button_previous?.background = ContextCompat.getDrawable(itemView.context, background)
                ninchat_button_previous?.visibility = if (visible) View.VISIBLE else View.GONE
                ninchat_button_previous?.text = text
            }
        }
    }

    override fun onNextNextUpdated(visible: Boolean, text: String?, imageButton: Boolean, clicked: Boolean, enabled: Boolean) {
        val background = if (enabled) if (clicked) R.drawable.ninchat_chat_primary_oncliked_button else R.drawable.ninchat_chat_primary_button else R.drawable.ninchat_chat_disable_button
        itemView.run {
            isEnabled = enabled
            if (imageButton) {
                ninchat_image_button_next?.isEnabled = enabled
                ninchat_image_button_next?.visibility = if (visible) View.VISIBLE else View.GONE
                ninchat_image_button_next?.background = ContextCompat.getDrawable(itemView.context, background)
            } else {
                ninchat_button_next?.isEnabled = enabled
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