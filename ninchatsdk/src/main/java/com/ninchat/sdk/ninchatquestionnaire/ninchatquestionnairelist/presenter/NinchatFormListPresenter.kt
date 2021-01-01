package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view.QuestionnaireActivityCallback
import org.json.JSONObject

class NinchatFormListPresenter(
        questionnaireList: List<JSONObject>,
        var rootActivityCallback: QuestionnaireActivityCallback,
) : NinchatQuestionnaireListPresenter (questionnaireList = questionnaireList) {


    init {
        // try to get the first element
        val nextElement = getNextElement(currentIndex = 0, 100)
        nextElement?.let {
            loadNextByElement(elementName = it)
        } ?: rootActivityCallback.onComplete(answerList = model.answerList)
    }

    override fun get(at: Int): JSONObject = model.answerList.takeLast(model.selectedElement.lastOrNull()?.second
            ?: 0).getOrNull(at) ?: JSONObject()

    override fun size(): Int = model.selectedElement.lastOrNull()?.second ?: 0

    override fun isLast(at: Int): Boolean = true

    override fun addThankYouView(isComplete: Boolean) {
        val thankYouText = if (isComplete) model.audienceRegisterCloseText() else model.audienceRegisterText()
        thankYouText?.let {
            val currentElement = NinchatQuestionnaireJsonUtil.getThankYouElement(thankYouString = it)
            model.addElement(jsonObject = currentElement)
            rootActivityCallback.onDataSetChange()
        } ?: rootActivityCallback.onFinishQuestionnaire(openQueue = false)
    }

    override fun showNext(onNextQuestionnaire: OnNextQuestionnaire?) {
        // if a thank you text
        if (onNextQuestionnaire?.moveType == OnNextQuestionnaire.thankYou) {
            rootActivityCallback.onFinishQuestionnaire(openQueue = false)
            return
        }
        if (onNextQuestionnaire?.moveType == OnNextQuestionnaire.back) {
            // todo
            return
        }
        // if the last answer has some error
        if (model.hasError()) {
            model.updateError()
            rootActivityCallback.onDataSetChange()
            return
        }

        val index = model.getIndex(elementName = model.selectedElement.lastOrNull()?.first)
        val nextTargetName = this.getNextElement(currentIndex = index + 1, 1000)
        if (nextTargetName == "_complete") {
            rootActivityCallback.onComplete(answerList = model.answerList)
            return
        }
        if (nextTargetName == "_register") {
            rootActivityCallback.onRegistered(answerList = model.answerList)
            return
        }
        nextTargetName?.let {
            loadNextByElement(elementName = nextTargetName)
            rootActivityCallback.onDataSetChange()
        } ?: rootActivityCallback.onComplete(answerList = model.answerList)
    }
}
