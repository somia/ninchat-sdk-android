package com.ninchat.sdk.ninchatquestionnaire.helper

import org.json.JSONObject

class NinchatQuestionnaireNavigator {
    companion object {
        /**
         * Get the next immediate element that is greater or equal than given element index
         */
        fun getCurrentElement(questionnaireList: List<JSONObject> = listOf(), index: Int = 0): JSONObject? {
            return questionnaireList
                    .filterIndexed { currentIndex, _ -> currentIndex >= index }
                    .find { NinchatQuestionnaireType.isElement(it) }
        }

        /**
         * Get the next immediate element that is greater than given element index
         */
        fun getNextElement(questionnaireList: List<JSONObject> = listOf(), index: Int = 0): JSONObject? {
            return questionnaireList
                    .filterIndexed { currentIndex, _ -> currentIndex >= index }
                    .find { NinchatQuestionnaireType.isElement(it) }
        }
    }
}