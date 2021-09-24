package com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.model.NinchatButtonViewModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.view.INinchatButtonViewHolder
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class NinchatButtonViewPresenter(
    jsonObject: JSONObject?,
    val iPresenter: INinchatButtonViewPresenter,
    position: Int,
    enabled: Boolean,
) : INinchatButtonViewHolder {
    private var model = NinchatButtonViewModel(position = position, enabled = enabled).apply {
        parse(jsonObject = jsonObject)
    }

    fun updateModel(jsonObject: JSONObject?, enabled: Boolean) {
        model.update(enabled = enabled)
    }

    fun renderCurrentView() {
        handleBackButton(
            visibleImageButton = model.showPreviousImageButton,
            visibleTextButton = model.showPreviousTextButton,
            text = model.previousButtonLabel,
            clicked = model.previousButtonClicked,
            enabled = model.enabled
        )

        handleNextButton(
            visibleImageButton = model.showNextImageButton,
            visibleTextButton = model.showNextTextButton,
            text = model.nextButtonLabel,
            clicked = model.nextButtonClicked,
            enabled = model.enabled
        )
    }

    fun updateCurrentView() {
        handleBackButton(
            visibleImageButton = model.showPreviousImageButton,
            visibleTextButton = model.showPreviousTextButton,
            text = model.previousButtonLabel,
            clicked = model.previousButtonClicked,
            enabled = model.enabled
        )

        handleNextButton(
            visibleImageButton = model.showNextImageButton,
            visibleTextButton = model.showNextTextButton,
            text = model.nextButtonLabel,
            clicked = model.nextButtonClicked,
            enabled = model.enabled
        )
    }

    override fun onBackButtonClicked() {
        model.previousButtonClicked = !model.previousButtonClicked

        handleBackButton(
            visibleImageButton = model.showPreviousImageButton,
            visibleTextButton = model.showPreviousTextButton,
            text = model.previousButtonLabel,
            clicked = model.previousButtonClicked,
            enabled = model.enabled
        )

        // may be sent event that back is clicked
        mayBeFireEvent(isBack = true)
    }


    override fun onNextButtonClicked() {
        model.nextButtonClicked = !model.nextButtonClicked

        handleNextButton(
            visibleImageButton = model.showNextImageButton,
            visibleTextButton = model.showNextTextButton,
            text = model.nextButtonLabel,
            clicked = model.nextButtonClicked,
            enabled = model.enabled
        )

        // may be sent event that next is clicked
        mayBeFireEvent(isBack = false)
    }

    private fun handleBackButton(
        visibleImageButton: Boolean = false,
        visibleTextButton: Boolean = false,
        text: String?,
        clicked: Boolean = false,
        enabled: Boolean
    ) {
        iPresenter.run {
            // prepare and post process data for back view
            onBackButtonUpdated(
                visible = visibleImageButton,
                text = text,
                imageButton = true,
                clicked = clicked,
                enabled = enabled
            )
            onBackButtonUpdated(
                visible = visibleTextButton,
                text = text,
                imageButton = false,
                clicked = clicked,
                enabled = enabled
            )
        }
    }

    private fun handleNextButton(
        visibleImageButton: Boolean = false,
        visibleTextButton: Boolean = false,
        text: String?,
        clicked: Boolean = false,
        enabled: Boolean
    ) {
        iPresenter.run {
            // prepare and post process data for back view
            onNextNextUpdated(
                visible = visibleImageButton,
                text = text,
                imageButton = true,
                clicked = clicked,
                enabled = enabled
            )
            onNextNextUpdated(
                visible = visibleTextButton,
                text = text,
                imageButton = false,
                clicked = clicked,
                enabled = enabled
            )
        }
    }

    private fun mayBeFireEvent(isBack: Boolean) {
        if (!model.fireEvent) return

        if (model.isThankYouText) {
            EventBus.getDefault().post(OnNextQuestionnaire(OnNextQuestionnaire.thankYou))
            return
        }
        EventBus.getDefault()
            .post(OnNextQuestionnaire(if (isBack) OnNextQuestionnaire.back else OnNextQuestionnaire.forward))
    }

    fun isSingleButton(): Boolean {
        val buttonCount = listOf<Boolean>(
            model.showPreviousImageButton,
            model.showPreviousTextButton,
            model.showNextImageButton,
            model.showNextTextButton
        ).sumBy {
            if (it) 1 else 0
        }
        return buttonCount == 1
    }

    fun imageButton(): Boolean = model.showPreviousImageButton || model.showNextImageButton

}

interface INinchatButtonViewPresenter {
    fun onBackButtonUpdated(
        visible: Boolean = false,
        text: String?,
        imageButton: Boolean = true,
        clicked: Boolean = false,
        enabled: Boolean
    )

    fun onNextNextUpdated(
        visible: Boolean = false,
        text: String?,
        imageButton: Boolean = true,
        clicked: Boolean = false,
        enabled: Boolean
    )
}