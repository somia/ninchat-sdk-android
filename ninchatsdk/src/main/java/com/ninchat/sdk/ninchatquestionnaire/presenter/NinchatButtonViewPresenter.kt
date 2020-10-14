package com.ninchat.sdk.ninchatquestionnaire.presenter

import android.view.View
import com.ninchat.sdk.ninchatquestionnaire.model.NinchatButtonViewModel
import kotlinx.android.synthetic.main.control_buttons.view.*
import org.json.JSONObject

class NinchatButtonViewPresenter(
        var ninchatButtonViewModel: NinchatButtonViewModel,
) {

    fun updateViewModel(_ninchatButtonViewModel: NinchatButtonViewModel) {
        ninchatButtonViewModel = _ninchatButtonViewModel.copy()
    }

    fun initiateView(itemView: View) {
        // initially all view should be invisible
        listOf(
                itemView.ninchat_image_button_previous,
                itemView.ninchat_button_previous,
                itemView.ninchat_image_button_next,
                itemView.ninchat_button_next).forEach { it.visibility == View.GONE }
    }

    fun renderCurrentView(itemView: View) {
        // handle previous button view
        when {
            ninchatButtonViewModel.showPreviousImageButton -> {
                itemView.ninchat_image_button_previous.visibility = View.VISIBLE
                itemView.ninchat_image_button_previous.setOnClickListener {
                }
            }
            ninchatButtonViewModel.showPreviousTextButton -> {
                itemView.ninchat_button_previous.visibility = View.VISIBLE
                itemView.ninchat_button_previous.text = ninchatButtonViewModel.previousButtonLabel
                itemView.ninchat_button_previous.setOnClickListener {
                }
            }
        }

        // handle next button view
        when {
            ninchatButtonViewModel.showNextImageButton -> {
                itemView.ninchat_image_button_next.visibility = View.VISIBLE
                itemView.ninchat_image_button_next.setOnClickListener {
                }
            }
            ninchatButtonViewModel.showNextTextButton -> {
                itemView.ninchat_button_next.visibility = View.VISIBLE
                itemView.ninchat_button_next.text = ninchatButtonViewModel.nextButtonLabel
                itemView.ninchat_button_next.setOnClickListener {
                }
            }
        }
    }

    companion object {
        fun parseJson(jsonObject: JSONObject?): NinchatButtonViewModel {
            return NinchatButtonViewModel()
        }
    }
}