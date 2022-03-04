package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view.QuestionnaireActivityCallback
import org.json.JSONObject

class NinchatFormListPresenter(
        questionnaireList: List<JSONObject>,
        preAnswers: List<Pair<String, Any>>,
        var rootActivityCallback: QuestionnaireActivityCallback,
) : NinchatQuestionnaireListPresenter(questionnaireList = questionnaireList, preAnswers = preAnswers) {

    override fun init() {
        // try to get the first element
        val nextElement = getNextElement(currentIndex = 0, 100)
        nextElement?.let {
            loadNextByElementName(elementName = it)
        } ?: rootActivityCallback.onComplete(answerList = getAnswerList())
    }

    override fun addThankYouView(isComplete: Boolean) {
        val thankYouText = if (isComplete) model.audienceRegisterCloseText() else model.audienceRegisterText()
        thankYouText?.let {
            val currentElement = NinchatQuestionnaireJsonUtil.getThankYouElement(thankYouString = it)
            model.addElement(jsonObject = currentElement)
            rootActivityCallback.onDataSetChange(withError = false)
        } ?: rootActivityCallback.onFinishQuestionnaire(openQueue = false)
    }

    override fun showNext(onNextQuestionnaire: OnNextQuestionnaire?) {
        // if a thank you text
        if (onNextQuestionnaire?.moveType == OnNextQuestionnaire.thankYou) {
            rootActivityCallback.onFinishQuestionnaire(openQueue = false)
            return
        }
        if (onNextQuestionnaire?.moveType == OnNextQuestionnaire.back) {
            // Remove last questionnaire element, and associate bot view element
            model.removeLast()
            val lastItemCount = model.selectedElement.lastOrNull()?.second ?: 0
            // reset answers element
            // model.answerList = model.resetAnswers(from = model.answerList.size - lastItemCount - 1)
            rootActivityCallback.onDataSetChange(withError = false)
            return
        }
        // if the last answer has some error
        if (model.hasError()) {
            model.updateError()
            rootActivityCallback.onDataSetChange(withError = true)
            return
        }

        val index = model.getIndex(elementName = model.selectedElement.lastOrNull()?.first)
        val nextTargetName = getNextElement(currentIndex = index + 1, 1000)
        when (nextTargetName) {
            "_complete" -> {
                rootActivityCallback.onComplete(answerList = getAnswerList())
                return
            }
            "_register" -> {
                rootActivityCallback.onRegistered(answerList = getAnswerList())
                return
            }
            "_close" -> {
                rootActivityCallback.onFinishQuestionnaire(openQueue = false)
                return
            }
        }
        // otherwise simply load next element by target name or if there is no target then treat as _complete
        nextTargetName?.let {
            loadNextByElementName(elementName = nextTargetName)
            rootActivityCallback.onDataSetChange(withError = false)
        } ?: rootActivityCallback.onComplete(answerList = getAnswerList())
    }

    override fun isLast(at: Int): Boolean = true

    override fun size(): Int = model.selectedElement.lastOrNull()?.second ?: 0

    // For form like questionnaire always show the last n elements only
    override fun getByMuskedPosition(index: Int): JSONObject = model.answerList.takeLast(model.selectedElement.lastOrNull()?.second
            ?: 0).getOrNull(index) ?: JSONObject()

}
