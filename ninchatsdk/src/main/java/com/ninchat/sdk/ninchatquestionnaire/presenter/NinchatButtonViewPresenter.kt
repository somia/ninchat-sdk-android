package com.ninchat.sdk.ninchatquestionnaire.presenter

import com.ninchat.sdk.ninchatquestionnaire.model.NinchatButtonViewModel
import com.ninchat.sdk.ninchatquestionnaire.view.INinchatButtonViewHolder
import org.json.JSONObject

class NinchatButtonViewPresenter(
        var ninchatButtonViewModel: NinchatButtonViewModel,
        val iPresenter: INinchatButtonViewPresenter,
) : INinchatButtonViewHolder {

    fun updateViewModel(_ninchatButtonViewModel: NinchatButtonViewModel) {
        ninchatButtonViewModel = _ninchatButtonViewModel.copy()
    }

    fun renderCurrentView() {
        handleBackButton(visibleImageButton = ninchatButtonViewModel.showPreviousImageButton,
                visibleTextButton = ninchatButtonViewModel.showPreviousTextButton,
                text = ninchatButtonViewModel.previousButtonLabel,
                enabled = ninchatButtonViewModel.backButtonEnabled)

        handleNextButton(visibleImageButton = ninchatButtonViewModel.showNextImageButton,
                visibleTextButton = ninchatButtonViewModel.showNextTextButton,
                text = ninchatButtonViewModel.nextButtonLabel,
                enabled = ninchatButtonViewModel.nextButtonEnabled)

    }

    override fun onBackButtonClicked() {
        // may be sent event that back is clicked
        ninchatButtonViewModel.backButtonEnabled = !ninchatButtonViewModel.backButtonEnabled

        handleBackButton(visibleImageButton = ninchatButtonViewModel.showPreviousImageButton,
                visibleTextButton = ninchatButtonViewModel.showPreviousTextButton,
                text = ninchatButtonViewModel.previousButtonLabel,
                enabled = ninchatButtonViewModel.backButtonEnabled)
    }


    override fun onNextButtonClicked() {
        ninchatButtonViewModel.nextButtonEnabled = !ninchatButtonViewModel.nextButtonEnabled

        // may be sent event that next is clicked
        handleNextButton(visibleImageButton = ninchatButtonViewModel.showNextImageButton,
                visibleTextButton = ninchatButtonViewModel.showNextTextButton,
                text = ninchatButtonViewModel.nextButtonLabel,
                enabled = ninchatButtonViewModel.nextButtonEnabled)
    }

    private fun handleBackButton(visibleImageButton: Boolean = false, visibleTextButton: Boolean = false, text: String?, enabled: Boolean = false) {
        iPresenter.run {
            // prepare and post process data for back view
            onBackButtonUpdated(visible = visibleImageButton, text = text, imageButton = true, enabled = enabled)
            onBackButtonUpdated(visible = visibleTextButton, text = text, imageButton = false, enabled = enabled)
        }
    }

    private fun handleNextButton(visibleImageButton: Boolean = false, visibleTextButton: Boolean = false, text: String?, enabled: Boolean = false) {
        iPresenter.run {
            // prepare and post process data for back view
            onNextNextUpdated(visible = visibleImageButton, text = text, imageButton = true, enabled = enabled)
            onNextNextUpdated(visible = visibleTextButton, text = text, imageButton = false, enabled = enabled)
        }
    }

    companion object {
        fun parseJson(jsonObject: JSONObject?): NinchatButtonViewModel {
            return NinchatButtonViewModel()
        }
    }
}

interface INinchatButtonViewPresenter {
    fun onBackButtonUpdated(visible: Boolean = false, text: String?, imageButton: Boolean = true, enabled: Boolean = false)
    fun onNextNextUpdated(visible: Boolean = false, text: String?, imageButton: Boolean = true, enabled: Boolean = false)
}