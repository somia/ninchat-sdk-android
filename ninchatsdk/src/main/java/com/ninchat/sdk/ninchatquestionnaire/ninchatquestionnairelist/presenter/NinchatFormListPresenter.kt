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
            model.removeLast()
            rootActivityCallback.onDataSetChange()
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

    override fun mapPosition(position: Int): Int {
        // real position in the answer list from the relative flat data structure
        /*
        3 5 9 12 14
        0 0 0 0  0
        1 1 1 1  1
        2   2 2
              3

        0 1 2 3 4 5 6 7 8 9 10 11  12 13
        0 1 2 0 1 0 1 2 0 1 2  3   0   1

        pos = totalSize - lastElementSize + relativePosition
        */
        return model.answerList.size - (model.selectedElement.lastOrNull()?.second ?: 0) + position
    }
}
