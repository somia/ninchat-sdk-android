package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter

import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.model.NinchatQuestionnaireListModel

class NinchatQuestionnaireListPresenter(questionnaireType: Int) {
    val model = NinchatQuestionnaireListModel(questionnaireType = questionnaireType)

    fun size() = 0
}