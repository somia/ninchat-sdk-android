package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireType
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.presenter.CheckboxUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.presenter.DropDownSelectUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfield.presenter.InputFieldUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view.QuestionnaireActivityCallback
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.model.ConversationLikeModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.model.FormLikeModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter.ButtonListUpdateListener
import org.json.JSONObject

class NinchatQuestionnaireListPresenter(
        questionnaireList: List<JSONObject>,
        isFormLike: Boolean,
        var rootActivityCallback: QuestionnaireActivityCallback,
        val viewCallback: INinchatQuestionnaireListPresenter,
) : InputFieldUpdateListener, ButtonListUpdateListener, DropDownSelectUpdateListener, CheckboxUpdateListener {
    val model =
            if (isFormLike) {
                FormLikeModel(
                        questionnaireList = questionnaireList,
                        answerList = listOf(),
                        selectedElement = arrayListOf(),
                        isFormLike = isFormLike
                ).apply {
                    parse()
                }
            } else {
                ConversationLikeModel(
                        questionnaireList = questionnaireList,
                        answerList = listOf(),
                        selectedElement = arrayListOf(),
                        isFormLike = isFormLike
                ).apply { parse() }
            }

    fun get(at: Int): JSONObject = model.get(at)
    fun size() = model.size()
    fun isLast(at: Int): Boolean = model.isLast(at)

    private fun loadNext(elementName: String?): Int {
        val index = model.getIndex(elementName = elementName)
        val nextElement = model.questionnaireList.getOrNull(index)
        return model.addElement(jsonObject = nextElement)
    }

    private fun loadThankYou(text: String): Int {
        val nextElement = NinchatQuestionnaireJsonUtil.getThankYouElement(thankYouString = text)
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
                    // todo(pallab) need to update answers with logic values ( tags, queueId )
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

    fun showNext(onNextQuestionnaire: OnNextQuestionnaire) {
        // get index of the element
        val index = model.getIndex(elementName = model.selectedElement.lastOrNull()?.first)
        val nextTargetName = this.getNextElement(currentIndex = index + 1, 10000)
        when {
            onNextQuestionnaire.moveType == OnNextQuestionnaire.back -> {
                val positionStart = size()
                val itemCount = model.removeLast()
                if (model.isFormLike) {
                    rootActivityCallback.onDataSetChange()
                } else {
                    viewCallback.onItemRemoved(positionStart = positionStart, itemCount = itemCount)
                }
                return
            }
            onNextQuestionnaire.moveType == OnNextQuestionnaire.thankYou -> {
                rootActivityCallback.onFinishQuestionnaire(openQueue = false)
                return
            }
            model.hasError() -> {
                val positionStart = size()
                val itemCount = model.selectedElement.lastOrNull()?.second ?: 0
                model.updateError()
                if (model.isFormLike) {
                    rootActivityCallback.onDataSetChange()
                } else {
                    viewCallback.onItemUpdate(positionStart = positionStart - itemCount, itemCount = itemCount)
                }
                return
            }
            nextTargetName == "_complete" -> {
                rootActivityCallback.onComplete(answerList = model.answerList)
                return
            }
            nextTargetName == "_register" -> {
                rootActivityCallback.onRegistered(answerList = model.answerList)
                return
            }
        }
        val positionStart = size()
        val itemCount = loadNext(elementName = nextTargetName)
        // there are still some item available
        if (itemCount > 0) {
            if (model.isFormLike) {
                rootActivityCallback.onDataSetChange()
            } else {
                viewCallback.onAddItem(positionStart = positionStart, itemCount = itemCount)
            }
        } else {
            // no more element. follow "_complete"
            rootActivityCallback.onComplete(answerList = model.answerList)
            return
        }
    }

    fun showThankYouText(isComplete: Boolean) {
        val thankYouText = if (isComplete) model.audienceRegisterCloseText() else model.audienceRegisterText()
        // if has thank you text then show thank you text
        // otherwise just call finish
        thankYouText?.let {
            val positionStart = size()
            val itemCount = loadThankYou(it)
            if (itemCount > 0) {
                if (model.isFormLike) {
                    rootActivityCallback.onDataSetChange()
                } else {
                    viewCallback.onAddItem(positionStart = positionStart, itemCount = itemCount)
                }
            }
            return
        } ?: rootActivityCallback.onFinishQuestionnaire(openQueue = false)
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

interface INinchatQuestionnaireListPresenter {
    fun onAddItem(positionStart: Int, itemCount: Int)
    fun onItemRemoved(positionStart: Int, itemCount: Int)
    fun onItemUpdate(positionStart: Int, itemCount: Int)
}