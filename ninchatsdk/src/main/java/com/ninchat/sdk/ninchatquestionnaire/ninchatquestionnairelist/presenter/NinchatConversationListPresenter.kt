package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireType
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.presenter.CheckboxUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.presenter.DropDownSelectUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.presenter.InputFieldUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view.QuestionnaireActivityCallback
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.model.NinchatQuestionnaireListModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter.ButtonListUpdateListener
import org.json.JSONObject

class NinchatConversationListPresenter(
        questionnaireList: List<JSONObject>,
        var rootActivityCallback: QuestionnaireActivityCallback,
        val viewCallback: INinchatConversationListPresenter,
) : InputFieldUpdateListener, ButtonListUpdateListener, DropDownSelectUpdateListener, CheckboxUpdateListener {

    var model = NinchatQuestionnaireListModel(
            questionnaireList = questionnaireList,
            answerList = listOf(),
            selectedElement = arrayListOf()
    )
    var botViewCallback: ((Int) -> Unit)? = null

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
            }
            addBotWritingView()
        } ?: rootActivityCallback.onComplete(answerList = model.answerList)
    }

    private fun loadNextByElement(elementName: String?): Int {
        val index = model.getIndex(elementName = elementName)
        val nextElement = model.questionnaireList.getOrNull(index)
        return model.addElement(jsonObject = nextElement)
    }

    // A sentinel value to make sure we don't fall into infinite loop
    private fun getNextElement(currentIndex: Int, sentinel: Int): String? {
        if (sentinel < 0) return null
        val currentElement = model.questionnaireList.getOrNull(currentIndex)
        return when {
            NinchatQuestionnaireType.isLogic(currentElement) -> {
                // is this logic a match for all any existing answer ?
                val matches = NinchatQuestionnaireJsonUtil.matchAnswerList(logicElement = currentElement, answerList = model.answerList)
                if (matches) {
                    model.updateTagsAndQueueId(currentElement)
                    currentElement?.optJSONObject("logic")?.optString("target")
                } else
                    this.getNextElement(currentIndex = currentIndex + 1, sentinel = sentinel - 1)
            }
            NinchatQuestionnaireType.isElement(currentElement) -> {
                currentElement?.optString("name")
            }
            else -> {
                // null(end of element), or not an element or logic
                null
            }
        }
    }

    private fun addBotWritingView() {
        val positionStart = size()
        val previousItemCount = model.selectedElement.lastOrNull()?.second ?: 0
        val currentElement = NinchatQuestionnaireJsonUtil.getBotElement(botName = model.getBotName(), botImgUrl = model.getBotAvatar())
        val itemCount = model.addElement(jsonObject = currentElement)
        viewCallback.onAddItem(positionStart = positionStart, itemCount = itemCount, previousItemCount = previousItemCount)
    }

    fun get(at: Int): JSONObject = model.answerList[at]

    fun size() = model.answerList.size

    fun isLast(at: Int): Boolean {
        val lastElementCount = model.selectedElement.lastOrNull()?.second ?: 0
        return at + lastElementCount >= model.answerList.size
    }

    fun addThankYouView(isComplete: Boolean) {
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
            }
            addBotWritingView()
        } ?: rootActivityCallback.onFinishQuestionnaire(openQueue = false)
    }

    fun showNext(onNextQuestionnaire: OnNextQuestionnaire?) {
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
            }
            addBotWritingView()
        } ?: rootActivityCallback.onComplete(answerList = model.answerList)
    }

    override fun onUpdate(value: String?, sublistPosition: Int, hasError: Boolean, position: Int) {
        model.answerList.getOrNull(position)?.apply {
            putOpt("result", value)
            putOpt("hasError", hasError)
            putOpt("position", sublistPosition)
        }
    }

    override fun onUpdate(value: String?, position: Int) {
        model.answerList.getOrNull(position)?.apply {
            putOpt("result", value)
        }
    }

    override fun onUpdate(value: Boolean, hasError: Boolean, position: Int) {
        model.answerList.getOrNull(position)?.apply {
            putOpt("result", value)
            putOpt("hasError", hasError)
        }
    }

    override fun onUpdate(value: String?, hasError: Boolean, position: Int) {
        model.answerList.getOrNull(position)?.apply {
            putOpt("result", value)
            putOpt("hasError", hasError)
        }
    }
}

interface INinchatConversationListPresenter {
    fun onAddItem(positionStart: Int, itemCount: Int, previousItemCount: Int)
    fun onItemRemoved(positionStart: Int, itemCount: Int)
    fun onItemUpdate(positionStart: Int, itemCount: Int)
}