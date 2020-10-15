package com.ninchat.sdk.ninchatquestionnaire.presenter

import com.ninchat.sdk.ninchatquestionnaire.model.NinchatButtonViewModel
import com.ninchat.sdk.ninchatquestionnaire.view.INinchatButtonViewHolder
import org.json.JSONObject

class NinchatButtonViewPresenter(
        jsonObject: JSONObject?,
        val iPresenter: INinchatButtonViewPresenter,
) : INinchatButtonViewHolder {
    private val ninchatButtonViewModel: NinchatButtonViewModel = NinchatButtonViewModel().parse(jsonObject = jsonObject)

    fun renderCurrentView() {
        handleBackButton(visibleImageButton = ninchatButtonViewModel.showPreviousImageButton,
                visibleTextButton = ninchatButtonViewModel.showPreviousTextButton,
                text = ninchatButtonViewModel.previousButtonLabel,
                clicked = ninchatButtonViewModel.previousButtonClicked)

        handleNextButton(visibleImageButton = ninchatButtonViewModel.showNextImageButton,
                visibleTextButton = ninchatButtonViewModel.showNextTextButton,
                text = ninchatButtonViewModel.nextButtonLabel,
                clicked = ninchatButtonViewModel.nextButtonClicked)

    }

    override fun onBackButtonClicked() {
        // may be sent event that back is clicked
        ninchatButtonViewModel.previousButtonClicked = !ninchatButtonViewModel.previousButtonClicked

        handleBackButton(visibleImageButton = ninchatButtonViewModel.showPreviousImageButton,
                visibleTextButton = ninchatButtonViewModel.showPreviousTextButton,
                text = ninchatButtonViewModel.previousButtonLabel,
                clicked = ninchatButtonViewModel.previousButtonClicked)
    }


    override fun onNextButtonClicked() {
        ninchatButtonViewModel.nextButtonClicked = !ninchatButtonViewModel.nextButtonClicked

        // may be sent event that next is clicked
        handleNextButton(visibleImageButton = ninchatButtonViewModel.showNextImageButton,
                visibleTextButton = ninchatButtonViewModel.showNextTextButton,
                text = ninchatButtonViewModel.nextButtonLabel,
                clicked = ninchatButtonViewModel.nextButtonClicked)
    }

    private fun handleBackButton(visibleImageButton: Boolean = false, visibleTextButton: Boolean = false, text: String?, clicked: Boolean = false) {
        iPresenter.run {
            // prepare and post process data for back view
            onBackButtonUpdated(visible = visibleImageButton, text = text, imageButton = true, clicked = clicked)
            onBackButtonUpdated(visible = visibleTextButton, text = text, imageButton = false, clicked = clicked)
        }
    }

    private fun handleNextButton(visibleImageButton: Boolean = false, visibleTextButton: Boolean = false, text: String?, clicked: Boolean = false) {
        iPresenter.run {
            // prepare and post process data for back view
            onNextNextUpdated(visible = visibleImageButton, text = text, imageButton = true, clicked = clicked)
            onNextNextUpdated(visible = visibleTextButton, text = text, imageButton = false, clicked = clicked)
        }
    }
}

interface INinchatButtonViewPresenter {
    fun onBackButtonUpdated(visible: Boolean = false, text: String?, imageButton: Boolean = true, clicked: Boolean = false)
    fun onNextNextUpdated(visible: Boolean = false, text: String?, imageButton: Boolean = true, clicked: Boolean = false)
}