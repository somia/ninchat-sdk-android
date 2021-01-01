package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.model

import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireNavigator
import com.ninchat.sdk.ninchatquestionnaire.helper.fromJSONArray
import org.json.JSONObject

data class NinchatQuestionnaireListModel(
        var questionnaireList: List<JSONObject> = listOf(),
        var answerList: List<JSONObject> = listOf(),
        var selectedElement: ArrayList<Pair<String, Int>> = arrayListOf(),
) {

    fun getBotName(): String? {
        return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getQuestionnaireName()
    }

    fun getBotAvatar(): String? {
        return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getQuestionnaireAvatar()
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

    fun updateTagsAndQueueId(logicElement: JSONObject?) {
        val tags = logicElement?.optJSONArray("tags")
        val queueId = logicElement?.optString("queue", logicElement.optString("queueId"))

        answerList.lastOrNull()?.apply {
            queueId?.let {
                putOpt("queueId", it)
            }
            tags?.let {
                putOpt("tags", it)
            }
        }
    }

    fun getIndex(elementName: String?): Int {
        // check if we can found the index for given element
        return NinchatQuestionnaireNavigator.getElementIndex(questionnaireList = questionnaireList,
                elementName = elementName ?: "~")
    }

    fun hasError(): Boolean {
        return answerList.takeLast(selectedElement.lastOrNull()?.second ?: 0).any {
            // is required but there is no result
            !NinchatQuestionnaireJsonUtil.requiredOk(json = it) || !NinchatQuestionnaireJsonUtil.matchPattern(json = it)
        }
    }

    fun audienceRegisterText(): String? =
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getAudienceRegisteredText()

    fun audienceRegisterCloseText(): String? =
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getAudienceRegisteredClosedText()
}
