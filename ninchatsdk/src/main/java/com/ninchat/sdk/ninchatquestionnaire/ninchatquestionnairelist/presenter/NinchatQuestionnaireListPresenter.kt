package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter

import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.model.NinchatQuestionnaireListModel
import com.ninchat.sdk.ninchatqueuelist.model.NinchatQueue
import org.json.JSONObject

class NinchatQuestionnaireListPresenter(questionnaireType: Int) {
    val model = NinchatQuestionnaireListModel(questionnaireType = questionnaireType).apply {
        parse()
    }

    fun get(at: Int): JSONObject = model.questionnaireList.getOrNull(at) ?: JSONObject()
    fun size() = model.questionnaireList.size
}