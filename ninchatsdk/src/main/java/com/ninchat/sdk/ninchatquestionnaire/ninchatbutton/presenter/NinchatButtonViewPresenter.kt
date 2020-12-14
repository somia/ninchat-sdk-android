package com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.model.NinchatButtonViewModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.view.INinchatButtonViewHolder
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class NinchatButtonViewPresenter(
        jsonObject: JSONObject?,
        val iPresenter: INinchatButtonViewPresenter,
        position: Int
) : INinchatButtonViewHolder {
    private var ninchatButtonViewModel = NinchatButtonViewModel(position = position).apply {
        parse(jsonObject = jsonObject)
    }

    fun updateModel(jsonObject: JSONObject?) {
        // ninchatButtonViewModel.parse(jsonObject = jsonObject)
    }

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
        ninchatButtonViewModel.previousButtonClicked = !ninchatButtonViewModel.previousButtonClicked

        handleBackButton(visibleImageButton = ninchatButtonViewModel.showPreviousImageButton,
                visibleTextButton = ninchatButtonViewModel.showPreviousTextButton,
                text = ninchatButtonViewModel.previousButtonLabel,
                clicked = ninchatButtonViewModel.previousButtonClicked)

        // may be sent event that back is clicked
        mayBeFireEvent(isBack = true)
    }


    override fun onNextButtonClicked() {
        ninchatButtonViewModel.nextButtonClicked = !ninchatButtonViewModel.nextButtonClicked

        handleNextButton(visibleImageButton = ninchatButtonViewModel.showNextImageButton,
                visibleTextButton = ninchatButtonViewModel.showNextTextButton,
                text = ninchatButtonViewModel.nextButtonLabel,
                clicked = ninchatButtonViewModel.nextButtonClicked)

        // may be sent event that next is clicked
        mayBeFireEvent(isBack = false)
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

    private fun mayBeFireEvent(isBack: Boolean) {
        if (!ninchatButtonViewModel.fireEvent) return

        if (ninchatButtonViewModel.isThankYouText) {
            EventBus.getDefault().post(OnNextQuestionnaire(OnNextQuestionnaire.thankYou))
            return
        }
        EventBus.getDefault().post(OnNextQuestionnaire(if (isBack) OnNextQuestionnaire.back else OnNextQuestionnaire.forward))
    }
}

interface INinchatButtonViewPresenter {
    fun onBackButtonUpdated(visible: Boolean = false, text: String?, imageButton: Boolean = true, clicked: Boolean = false)
    fun onNextNextUpdated(visible: Boolean = false, text: String?, imageButton: Boolean = true, clicked: Boolean = false)
}