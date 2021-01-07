package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireType
import com.ninchat.sdk.ninchatquestionnaire.ninchatbotwriting.presenter.BotWritingCompleteListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.presenter.CheckboxUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.presenter.DropDownSelectUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfieldviewholder.presenter.InputFieldUpdateListener
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.model.NinchatQuestionnaireListModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter.ButtonListUpdateListener
import org.json.JSONObject

open class NinchatQuestionnaireListPresenter(
        questionnaireList: List<JSONObject>,
        preAnswers: List<Pair<String, Any>>,
) : InputFieldUpdateListener, ButtonListUpdateListener, DropDownSelectUpdateListener, CheckboxUpdateListener, BotWritingCompleteListener {

    var model = NinchatQuestionnaireListModel(
            questionnaireList = questionnaireList,
            answerList = listOf(),
            selectedElement = arrayListOf()
    ).apply {
        withPreAnswers(preAnswers = preAnswers)
    }

    override fun onUpdate(value: String?, sublistPosition: Int, hasError: Boolean, position: Int) {
        model.answerList.getOrNull(position)?.apply {
            value?.let {
                putOpt("result", value)
            } ?: remove("result")
            putOpt("hasError", hasError)
            putOpt("position", sublistPosition)
        }
    }

    override fun onUpdate(value: String?, position: Int, hasError: Boolean) {
        model.answerList.getOrNull(position)?.apply {
            value?.let {
                putOpt("result", value)
            } ?: remove("result")
            putOpt("hasError", hasError)
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
            value?.let {
                putOpt("result", value)
            } ?: remove("result")
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
                val matches = NinchatQuestionnaireJsonUtil.matchAnswerList(logicElement = currentElement, answerList = model.answerList, preAnswerList = model.preAnswers)
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

    protected fun loadNextByElementName(elementName: String?): Int {
        val index = model.getIndex(elementName = elementName)
        val nextElement = model.questionnaireList.getOrNull(index)
        return model.addElement(jsonObject = nextElement)
    }


    fun getAnswerList(): List<JSONObject> {
        val uniquePreAnswers = model.preAnswers.filter { currentAnswer ->
            val name = currentAnswer.optString("name")
            model.answerList.any {
                name == it.optString("name")
            }.not()
        }
        return model.answerList.plus(uniquePreAnswers)
    }

    override fun onCompleteLoading(target: String?, thankYouText: String?, loaded: Boolean, position: Int) {
        model.answerList.getOrNull(position)?.apply {
            putOpt("loaded", true)
        }
    }

    open fun init() {
        TODO("implement me")
    }

    open fun addThankYouView(isComplete: Boolean) {
        TODO("implement me")
    }

    open fun showNext(onNextQuestionnaire: OnNextQuestionnaire?) {
        TODO("implement me")
    }

    open fun isLast(at: Int): Boolean {
        TODO("implement me")
    }

    open fun size(): Int {
        TODO("implement me")
    }

    open fun getIndexByUuid(uuid: Int): Int = model.answerList.indexOfFirst {
        it.optInt("uuid") == uuid
    }

    open fun getByIndexPosition(index: Int): JSONObject =
            model.answerList.getOrNull(index)
                    ?: JSONObject()

    open fun getByMuskedPosition(index: Int): JSONObject {
        TODO("implement me")
    }
}
