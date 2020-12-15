package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.model

import org.json.JSONObject

class ConversationLikeModel(
        questionnaireList: List<JSONObject>,
        answerList: List<JSONObject>,
        selectedElement: ArrayList<Pair<String, Int>>,
        isFormLike: Boolean,
) : NinchatQuestionnaireListModel(
        questionnaireList = questionnaireList,
        answerList = answerList,
        selectedElement = selectedElement,
        isFormLike = isFormLike
) {
    override fun size(): Int =
            answerList.size

    override fun get(at: Int): JSONObject =
            answerList.getOrNull(at) ?: JSONObject()

    override fun isLast(at: Int): Boolean {
        // last block of questionnaire contains lastElementCount items
        val lastElementCount = selectedElement.lastOrNull()?.second ?: 0
        return at + lastElementCount >= answerList.size
    }
}