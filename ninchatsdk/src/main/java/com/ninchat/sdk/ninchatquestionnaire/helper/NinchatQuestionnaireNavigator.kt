package com.ninchat.sdk.ninchatquestionnaire.helper

import org.json.JSONObject

class NinchatQuestionnaireNavigator {
    companion object {

        /**
         * Get the next immediate element that is greater than given element index
         */
        fun getNextElement(questionnaireList: List<JSONObject> = listOf(), index: Int = 0): JSONObject? {
            return questionnaireList
                    .filterIndexed { currentIndex, _ -> currentIndex >= index }
                    .find { NinchatQuestionnaireType.isElement(it) }
        }

        /**
         * Get the next immediate element that is greater than given element index
         */
        fun getElementIndex(questionnaireList: List<JSONObject> = listOf(), elementName: String): Int {
            return questionnaireList
                    .indexOfFirst { it.optString("name") == elementName }
        }
    }
}