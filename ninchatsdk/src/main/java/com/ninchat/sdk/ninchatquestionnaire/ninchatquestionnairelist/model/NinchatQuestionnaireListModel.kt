package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.model

import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireNavigator
import com.ninchat.sdk.ninchatquestionnaire.helper.fromJSONArray
import org.json.JSONObject

open class NinchatQuestionnaireListModel(
        var questionnaireList: List<JSONObject> = listOf(),
        var answerList: List<JSONObject> = listOf(),
        var selectedElement: ArrayList<Pair<String, Int>> = arrayListOf(),
        var isFormLike: Boolean = true,
) {

    open fun parse() {
        // get first element
        val element = nextElement(0)
        // add that element
        addElement(element)
    }

    fun addElement(jsonObject: JSONObject?): Int {
        return jsonObject?.let { currentElement ->
            val nextElementList = fromJSONArray<JSONObject>(currentElement.optJSONArray("elements")).map { NinchatQuestionnaireJsonUtil.slowCopy(it as JSONObject) }
            answerList = answerList.plus(nextElementList)
            // add it in the selected element
            selectedElement.add(Pair(currentElement.optString("name"), nextElementList.size))
            return nextElementList.size
        } ?: 0
    }

    fun removeLast(): Int {
        return selectedElement.removeLastOrNull()?.let {
            answerList = answerList.dropLast(n = it.second)
            it.second
        } ?: 0
    }

    fun updateError() {
        answerList = NinchatQuestionnaireJsonUtil.updateError(answerList = answerList,
                selectedElement = selectedElement.lastOrNull() ?: Pair("", 0))
    }

    fun getIndex(elementName: String?): Int {
        // check if we can found the index for given element
        val index = NinchatQuestionnaireNavigator.getElementIndex(questionnaireList = questionnaireList,
                elementName = elementName ?: "~")
        // if there is a match return the index
        // otherwise fetch the next element
        return if (index != -1)
            index - 1
        else
            NinchatQuestionnaireNavigator.getElementIndex(questionnaireList = questionnaireList,
                    elementName = selectedElement.lastOrNull()?.first ?: "~")
    }

    fun hasError(): Boolean {
        return answerList.takeLast(selectedElement.lastOrNull()?.second ?: 0).any {
            // is required but there is no result
            !NinchatQuestionnaireJsonUtil.requiredOk(json = it) || !NinchatQuestionnaireJsonUtil.matchPattern(json = it)
        }
    }

    fun hasMatch(elementName: String?): Boolean {
        val index = NinchatQuestionnaireNavigator.getElementIndex(questionnaireList = questionnaireList,
                elementName = elementName ?: "~")
        return if (index >= 0)
            true
        else
            NinchatQuestionnaireNavigator.getElementIndex(questionnaireList = questionnaireList,
                    elementName = selectedElement.lastOrNull()?.first ?: "~") >= 0
    }

    fun nextElement(index: Int = 0): JSONObject? =
            NinchatQuestionnaireNavigator.getNextElement(questionnaireList = questionnaireList, index = index)

    fun audienceRegisterText(): String? =
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getAudienceRegisteredText()

    fun audienceRegisterCloseText(): String? =
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getAudienceRegisteredClosedText()

    open fun size(): Int = 0
    open fun get(at: Int): JSONObject = JSONObject()
}
