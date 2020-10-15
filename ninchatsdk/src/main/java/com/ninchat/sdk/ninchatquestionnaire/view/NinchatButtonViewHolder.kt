package com.ninchat.sdk.ninchatquestionnaire.view

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatquestionnaire.model.NinchatButtonViewModel
import com.ninchat.sdk.ninchatquestionnaire.presenter.INinchatButtonViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.presenter.NinchatButtonViewPresenter
import kotlinx.android.synthetic.main.control_buttons.view.*

class NinchatButtonViewHolder(
        itemView: View,
        ninchatButtonViewModel: NinchatButtonViewModel,
) : RecyclerView.ViewHolder(itemView), INinchatButtonViewPresenter {
    val ninchatButtonViewPresenter = NinchatButtonViewPresenter(
            ninchatButtonViewModel = ninchatButtonViewModel,
            iPresenter = this
    )

    init {
        ninchatButtonViewPresenter.renderCurrentView()
        attachUserActionHancler()
    }

    fun update(ninchatButtonViewModel: NinchatButtonViewModel) {
        ninchatButtonViewPresenter.run {
            updateViewModel(_ninchatButtonViewModel = ninchatButtonViewModel)
            renderCurrentView()
        }
        attachUserActionHancler()
    }

    private fun attachUserActionHancler() {
        // update background of the button
        // update background of the button
        itemView.run {
            ninchat_image_button_previous?.setOnClickListener {
                ninchatButtonViewPresenter.onBackButtonClicked()
            }
            ninchat_button_previous?.setOnClickListener {
                ninchatButtonViewPresenter.onBackButtonClicked()
            }
            ninchat_image_button_next?.setOnClickListener {
                ninchatButtonViewPresenter.onBackButtonClicked()
            }
            ninchat_button_next?.setOnClickListener {
                ninchatButtonViewPresenter.onBackButtonClicked()
            }
        }
    }

    override fun onBackButtonUpdated(visible: Boolean, text: String?, imageButton: Boolean, enabled: Boolean) {
        val background = if (enabled) R.drawable.ninchat_chat_primary_oncliked_button else R.drawable.ninchat_chat_secondary_button
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

    override fun onNextNextUpdated(visible: Boolean, text: String?, imageButton: Boolean, enabled: Boolean) {
        val background = if (enabled) R.drawable.ninchat_chat_primary_oncliked_button else R.drawable.ninchat_chat_secondary_button
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