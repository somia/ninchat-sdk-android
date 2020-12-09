package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.model

import org.json.JSONObject

class FormLikeModel(
        questionnaireList: List<JSONObject>,
        answerList: List<JSONObject>,
        queueId: String?,
        selectedElement: ArrayList<Pair<String, Int>>,
        isFormLike: Boolean,
) : NinchatQuestionnaireListModel(
        questionnaireList = questionnaireList,
        answerList = answerList,
        queueId = queueId,
        selectedElement = selectedElement,
        isFormLike = isFormLike
) {
    override fun size(): Int {
        return selectedElement.lastOrNull()?.second ?: 0
    }

    override fun get(at: Int): JSONObject {
        return answerList
                .takeLast(selectedElement.lastOrNull()?.second ?: 0)
                .getOrNull(at) ?: JSONObject()
    }

}