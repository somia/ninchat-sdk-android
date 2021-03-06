package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.model

import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireNavigator
import com.ninchat.sdk.ninchatquestionnaire.helper.fromJSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

data class NinchatQuestionnaireListModel(
        var questionnaireList: List<JSONObject> = listOf(),
        var preAnswers: List<JSONObject> = listOf(),
        var answerList: List<JSONObject> = listOf(),
        var selectedElement: ArrayList<Pair<String, Int>> = arrayListOf(),
) {

    fun withPreAnswers(preAnswers: List<Pair<String, Any>> = listOf()) {
        this.preAnswers = preAnswers.map {
            JSONObject()
                    .putOpt("name", it.first)
                    .putOpt("result", it.second)
        }
    }

    fun getBotName(): String? {
        return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getQuestionnaireName()
    }

    fun getBotAvatar(): String? {
        return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getQuestionnaireAvatar()
    }

    fun addElement(jsonObject: JSONObject?): Int {
        return jsonObject?.let { currentElement ->
            val nextElementList = fromJSONArray<JSONObject>(currentElement.optJSONArray("elements")).map {
                NinchatQuestionnaireJsonUtil.slowCopy(it as JSONObject).apply {
                    putOpt("uuid", UUID.randomUUID().hashCode())
                }
            }
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


    fun updateTagsAndQueueId(logicElement: JSONObject?) {
        val tags = logicElement?.optJSONArray("tags")
        val queueId = logicElement?.optString("queue", logicElement.optString("queueId", null))

        answerList.lastOrNull()?.apply {
            remove("queueId")
            remove("tags")
            queueId?.let {
                if (it.isNotBlank()) {
                    putOpt("queueId", queueId)
                }
            }

            tags?.let {
                if (it.length() > 0)
                    putOpt("tags", it)
            }
        }
    }

    fun getIndex(elementName: String?): Int {
        // check if we can found the index for given element
        return NinchatQuestionnaireNavigator.getElementIndex(questionnaireList = questionnaireList,
                elementName = elementName ?: "~")
    }

    fun updateError() {
        answerList = NinchatQuestionnaireJsonUtil.updateError(answerList = answerList,
                selectedElement = selectedElement.lastOrNull())
    }

    fun hasError(): Boolean {
        return NinchatQuestionnaireJsonUtil.hasError(answerList = answerList, selectedElement = selectedElement.lastOrNull())
    }

    fun resetAnswers(from: Int): List<JSONObject> {
        return NinchatQuestionnaireJsonUtil.resetElements(answerList = answerList, from = from)
    }

    fun audienceRegisterText(): String? =
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getAudienceRegisteredText()

    fun audienceRegisterCloseText(): String? =
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getAudienceRegisteredClosedText()
}
