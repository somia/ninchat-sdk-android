package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.model

import android.util.Log
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireNavigator
import com.ninchat.sdk.ninchatquestionnaire.helper.fromJSONArray
import org.json.JSONObject

data class NinchatQuestionnaireListModel(
        var questionnaireList: List<JSONObject> = listOf(),
        var answerList: List<JSONObject> = listOf(),
        var currentIndex: Int = 0,
) {
    fun parse() {
        nextElement(index = currentIndex)
    }

    fun loadNext(): Int {
        currentIndex += 1
        return nextElement(index = currentIndex)
    }

    internal fun nextElement(index: Int = currentIndex): Int {
        val nextElement = NinchatQuestionnaireNavigator.getNextElement(questionnaireList = questionnaireList, index = index)
                ?: return 0

        val nextElementList = fromJSONArray<JSONObject>(nextElement.optJSONArray("elements"))
        answerList = answerList.plus(nextElementList).map { it as JSONObject }
        return nextElementList.size
    }

}