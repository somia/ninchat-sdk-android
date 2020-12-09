package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.model

import org.json.JSONObject

class ConversationLikeModel(
        questionnaireList: List<JSONObject>,
        answerList: List<JSONObject> ,
        queueId: String?,
        selectedElement: ArrayList<Pair<String, Int>>,
        isFormLike: Boolean ,
) : NinchatQuestionnaireListModel(
        questionnaireList = questionnaireList,
        answerList = answerList,
        queueId = queueId,
        selectedElement = selectedElement,
        isFormLike = isFormLike
) {
    override fun size(): Int =
            answerList.size

    override fun get(at: Int): JSONObject =
            answerList.getOrNull(at) ?: JSONObject()
}