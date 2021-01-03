package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view.QuestionnaireActivityCallback
import org.json.JSONObject

class NinchatConversationListPresenter(
        questionnaireList: List<JSONObject>,
        preAnswers: List<Pair<String,Any> >,
        var rootActivityCallback: QuestionnaireActivityCallback,
        val viewCallback: INinchatConversationListPresenter,
) : NinchatQuestionnaireListPresenter(questionnaireList = questionnaireList, preAnswers = preAnswers) {
    init {
        // try to get the first element
        val nextElement = getNextElement(currentIndex = 0, 100)
        nextElement?.let {
            botViewCallback = fun(position: Int) {
                botViewCallback = null
                model.answerList.getOrNull(position)?.apply {
                    putOpt("loaded", true)
                }
                val positionStart = size()
                val previousItemCount = model.selectedElement.lastOrNull()?.second ?: 0
                val itemCount = loadNextByElement(elementName = it)
                viewCallback.onAddItem(positionStart = positionStart, itemCount = itemCount, previousItemCount = previousItemCount)
                rootActivityCallback.scrollTo(position = positionStart)
            }
            addBotWritingView()
        } ?: rootActivityCallback.onComplete(answerList = model.answerList)
    }

    private fun addBotWritingView() {
        val positionStart = size()
        val previousItemCount = model.selectedElement.lastOrNull()?.second ?: 0
        val currentElement = NinchatQuestionnaireJsonUtil.getBotElement(botName = model.getBotName(), botImgUrl = model.getBotAvatar())
        val itemCount = model.addElement(jsonObject = currentElement)
        viewCallback.onAddItem(positionStart = positionStart, itemCount = itemCount, previousItemCount = previousItemCount)
        rootActivityCallback.scrollTo(position = positionStart)
    }

    override fun get(at: Int): JSONObject = model.answerList.getOrNull(at) ?: JSONObject()

    override fun size() = model.answerList.size

    override fun isLast(at: Int): Boolean {
        val lastElementCount = model.selectedElement.lastOrNull()?.second ?: 0
        return at + lastElementCount >= model.answerList.size
    }

    override fun addThankYouView(isComplete: Boolean) {
        val thankYouText = if (isComplete) model.audienceRegisterCloseText() else model.audienceRegisterText()
        thankYouText?.let {
            botViewCallback = fun(position: Int) {
                botViewCallback = null
                model.answerList.getOrNull(position)?.apply {
                    putOpt("loaded", true)
                }
                val positionStart = size()
                val previousItemCount = model.selectedElement.lastOrNull()?.second ?: 0
                val currentElement = NinchatQuestionnaireJsonUtil.getThankYouElement(thankYouString = it)
                val itemCount = model.addElement(jsonObject = currentElement)
                viewCallback.onAddItem(positionStart = positionStart, itemCount = itemCount, previousItemCount = previousItemCount)
                rootActivityCallback.scrollTo(position = positionStart)
            }
            addBotWritingView()
        } ?: rootActivityCallback.onFinishQuestionnaire(openQueue = false)
    }

    override fun showNext(onNextQuestionnaire: OnNextQuestionnaire?) {
        // if a thank you text
        if (onNextQuestionnaire?.moveType == OnNextQuestionnaire.thankYou) {
            rootActivityCallback.onFinishQuestionnaire(openQueue = false)
            return
        }
        if (onNextQuestionnaire?.moveType == OnNextQuestionnaire.back) {
            val positionStart = size()
            // remove last questionnaire element, and associate bot view element
            val itemCount = model.removeLast() + model.removeLast()
            viewCallback.onItemRemoved(positionStart = positionStart, itemCount = itemCount)
            return
        }
        // if the last answer has some error
        if (model.hasError()) {
            val positionStart = size()
            val itemCount = model.selectedElement.lastOrNull()?.second ?: 0
            model.updateError()
            viewCallback.onItemUpdate(positionStart = positionStart - itemCount, itemCount = itemCount)
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
            botViewCallback = fun(position: Int) {
                botViewCallback = null
                model.answerList.getOrNull(position)?.apply {
                    putOpt("loaded", true)
                }
                val positionStart = size()
                val previousItemCount = model.selectedElement.lastOrNull()?.second ?: 0
                val itemCount = loadNextByElement(elementName = nextTargetName)
                viewCallback.onAddItem(positionStart = positionStart, itemCount = itemCount, previousItemCount = previousItemCount)
                rootActivityCallback.scrollTo(position = positionStart)
            }
            addBotWritingView()
        } ?: rootActivityCallback.onComplete(answerList = model.answerList)
    }

    override fun mapPosition(position: Int): Int {
        return super.mapPosition(position)
    }
}

interface INinchatConversationListPresenter {
    fun onAddItem(positionStart: Int, itemCount: Int, previousItemCount: Int)
    fun onItemRemoved(positionStart: Int, itemCount: Int)
    fun onItemUpdate(positionStart: Int, itemCount: Int)
}