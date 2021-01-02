package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireType
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.presenter.CheckboxUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.presenter.DropDownSelectUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.presenter.InputFieldUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.model.NinchatQuestionnaireListModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter.ButtonListUpdateListener
import org.json.JSONObject

open class NinchatQuestionnaireListPresenter(
        questionnaireList: List<JSONObject>,
) : InputFieldUpdateListener, ButtonListUpdateListener, DropDownSelectUpdateListener, CheckboxUpdateListener {

    var botViewCallback: ((Int) -> Unit)? = null
    var model = NinchatQuestionnaireListModel(
            questionnaireList = questionnaireList,
            answerList = listOf(),
            selectedElement = arrayListOf()
    )

    override fun onUpdate(value: String?, sublistPosition: Int, hasError: Boolean, position: Int) {
        val at = mapPosition(position = position)
        model.answerList.getOrNull(at)?.apply {
            putOpt("result", value)
            putOpt("hasError", hasError)
            putOpt("position", sublistPosition)
        }
    }

    override fun onUpdate(value: String?, position: Int) {
        val at = mapPosition(position = position)
        model.answerList.getOrNull(at)?.apply {
            putOpt("result", value)
        }
    }

    override fun onUpdate(value: Boolean, hasError: Boolean, position: Int) {
        val at = mapPosition(position = position)
        model.answerList.getOrNull(at)?.apply {
            putOpt("result", value)
            putOpt("hasError", hasError)
        }
    }

    override fun onUpdate(value: String?, hasError: Boolean, position: Int) {
        val at = mapPosition(position = position)
        model.answerList.getOrNull(at)?.apply {
            putOpt("result", value)
            putOpt("hasError", hasError)
        }
    }

    // A sentinel value to make sure we don't fall into infinite loop
    protected fun getNextElement(currentIndex: Int, sentinel: Int): String? {
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

    protected fun loadNextByElement(elementName: String?): Int {
        val index = model.getIndex(elementName = elementName)
        val nextElement = model.questionnaireList.getOrNull(index)
        return model.addElement(jsonObject = nextElement)
    }


    open fun showNext(onNextQuestionnaire: OnNextQuestionnaire?) {}
    open fun addThankYouView(isComplete: Boolean) {}
    open fun isLast(at: Int): Boolean = false
    open fun size(): Int = 0
    open fun get(at: Int): JSONObject = JSONObject()
    open fun mapPosition(position: Int): Int = position
}
