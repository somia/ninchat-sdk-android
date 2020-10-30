package com.ninchat.sdk.ninchatquestionnaire.ninchatformquestionnaire.presenter

import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.common.interfaces.INinchatQuestionnairePresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatformquestionnaire.model.NinchatFormQuestionnaireModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatformquestionnaire.view.INinchatFormQuestionnaire
import org.json.JSONArray
import org.json.JSONObject

class NinchatFormQuestionnairePresenter(
        questionnaire: NinchatQuestionnaire? = null,
        val viewCallback: INinchatFormQuestionnaire,
) : INinchatQuestionnairePresenter {

    private val model = NinchatFormQuestionnaireModel(questionnaire = questionnaire)
    override fun addContent(questionnaireList: JSONObject?) {
        questionnaireList?.let {
            model.questionnaire?.addQuestionnaire(it)
        }
    }

    override fun updateContent(questionnaireList: JSONArray?) {
        questionnaireList?.let {
            model.questionnaire?.updateQuestionnaireList(it)
        }
    }

    override fun lastElement(): JSONObject? {
        return model.questionnaire?.lastElement
    }

    override fun secondLastElement(): JSONObject? {
        return model.questionnaire?.secondLastElement
    }

    override fun removeLast() {
        val position = size() - 1
        model.questionnaire?.removeQuestionnaireList(position)
        viewCallback.onRemovedElement(position)
    }

    override fun getQuestionnaireList(): JSONArray? {
        return model.questionnaire?.questionnaireList
    }

    override fun get(at: Int): JSONObject? {
        return model.questionnaire?.getItem(at)
    }

    override fun size(): Int {
        return model.questionnaire?.size() ?: 0
    }
}