package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view.QuestionnaireActivityCallback
import org.json.JSONObject

class NinchatConversationListPresenter(
        questionnaireList: List<JSONObject>,
        preAnswers: List<Pair<String, Any>>,
        var rootActivityCallback: QuestionnaireActivityCallback,
        val viewCallback: INinchatConversationListPresenter,
) : NinchatQuestionnaireListPresenter(questionnaireList = questionnaireList, preAnswers = preAnswers) {

    override fun init() {
        // try to get the first element
        val nextElement = getNextElement(currentIndex = 0, 100)
        // load bot view or if nothing found just finish
        nextElement?.let {
            addBotWritingView(nextTarget = it, thankYouText = null)
        } ?: rootActivityCallback.onComplete(answerList = getAnswerList())
    }

    private fun loadNextByTarget(targetName: String?) {
        val positionStart = size()
        val previousItemCount = model.selectedElement.lastOrNull()?.second ?: 0
        loadNextByElementName(elementName = targetName)
        viewCallback.onAddItem(positionStart = positionStart, lastItemCount = previousItemCount)
        rootActivityCallback.scrollTo(position = positionStart)
    }

    private fun loadNextByElement(elementName: JSONObject? = null) {
        val positionStart = size()
        val previousItemCount = model.selectedElement.lastOrNull()?.second ?: 0
        // add next element
        model.addElement(jsonObject = elementName)
        viewCallback.onAddItem(positionStart = positionStart, lastItemCount = previousItemCount)
        rootActivityCallback.scrollTo(position = positionStart)
    }

    private fun addBotWritingView(nextTarget: String?, thankYouText: String?) {
        val currentElement = NinchatQuestionnaireJsonUtil.getBotElement(botName = model.getBotName(), botImgUrl = model.getBotAvatar(), targetElement = nextTarget, thankYouText = thankYouText)
        loadNextByElement(elementName = currentElement)
    }

    override fun addThankYouView(isComplete: Boolean) {
        val thankYouText = if (isComplete) model.audienceRegisterCloseText() else model.audienceRegisterText()
        thankYouText?.let {
            addBotWritingView(nextTarget = "thankYouText", thankYouText = thankYouText)
        } ?: rootActivityCallback.onFinishQuestionnaire(openQueue = false)
    }

    override fun applyRegisteredView() {
        val index = model.getIndex(elementName = "_registered")
        val nextTargetName = getNextElement(currentIndex = index, 1000)
        // otherwise simply load next element by target name or if there is no target then treat as _complete
        nextTargetName?.let {
            addBotWritingView(nextTarget = nextTargetName, thankYouText = null)
        } ?: rootActivityCallback.onComplete(answerList = getAnswerList())
    }

    override fun applyCompletedView(skipView: Boolean) {
        if(skipView) {
            rootActivityCallback.onClose()
            return
        }
        val index = model.getIndex(elementName = "_completed")
        val nextTargetName = getNextElement(currentIndex = index, 1000)
        // otherwise simply load next element by target name or if there is no target then treat as _complete
        nextTargetName?.let {
            addBotWritingView(nextTarget = nextTargetName, thankYouText = null)
        } ?: rootActivityCallback.onComplete(answerList = getAnswerList())
    }

    override fun showNext(onNextQuestionnaire: OnNextQuestionnaire?) {
        // if a thank you text
        if (onNextQuestionnaire?.moveType == OnNextQuestionnaire.thankYou) {
            rootActivityCallback.onFinishQuestionnaire(openQueue = false)
            return
        }
        if (onNextQuestionnaire?.moveType == OnNextQuestionnaire.back) {
            val positionStart = size()
            // Remove last questionnaire element, and associate bot view element
            val totalRemoveCount = model.removeLast() + model.removeLast()
            val lastItemCount = model.selectedElement.lastOrNull()?.second ?: 0
            // reset answers element
            // model.answerList = model.resetAnswers(from = model.answerList.size - lastItemCount - 1)
            viewCallback.onItemRemoved(positionStart = positionStart - totalRemoveCount, totalItemCount = totalRemoveCount, lastItemCount = lastItemCount)
            return
        }
        // if the last answer has some error
        if (model.hasError()) {
            val positionStart = size() - 1
            val itemCount = model.selectedElement.lastOrNull()?.second ?: 0
            model.updateError()
            viewCallback.onItemUpdate(positionStart = positionStart - itemCount, totalItemCount = itemCount)
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
            addBotWritingView(nextTarget = nextTargetName, thankYouText = null)
        } ?: rootActivityCallback.onComplete(answerList = getAnswerList())
    }

    // override bot loading view
    override fun onCompleteLoading(target: String?, thankYouText: String?, loaded: Boolean, position: Int) {
        val isLoaded = loaded || model.answerList.getOrNull(position)?.optBoolean("loaded", false) == true
        when (target) {
            "thankYouText" -> {
                val nextElement = NinchatQuestionnaireJsonUtil.getThankYouElement(thankYouString = thankYouText
                        ?: "")
                loadNextByElement(elementName = nextElement)
            }
            else -> {
                if (isLoaded) return
                loadNextByTarget(targetName = target)
            }
        }
        super.onCompleteLoading(target = target, thankYouText, loaded = loaded, position = position)
    }

    override fun isLast(at: Int): Boolean {
        val lastElementCount = model.selectedElement.lastOrNull()?.second ?: 0
        return at + lastElementCount >= model.answerList.size
    }

    override fun size() = model.answerList.size

    override fun getByMuskedPosition(index: Int): JSONObject {
        return model.answerList.getOrNull(index) ?: JSONObject()
    }
}

interface INinchatConversationListPresenter {
    fun onAddItem(positionStart: Int, lastItemCount: Int)
    fun onItemRemoved(positionStart: Int, totalItemCount: Int, lastItemCount: Int)
    fun onItemUpdate(positionStart: Int, totalItemCount: Int)
}