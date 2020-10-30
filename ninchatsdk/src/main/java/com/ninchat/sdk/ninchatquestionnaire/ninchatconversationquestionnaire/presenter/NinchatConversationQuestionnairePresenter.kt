package com.ninchat.sdk.ninchatquestionnaire.ninchatconversationquestionnaire.presenter

import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.common.interfaces.INinchatQuestionnairePresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatconversationquestionnaire.model.NinchatConversationQuestionnaireModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatconversationquestionnaire.view.INinchatConversationQuestionnaire
import org.json.JSONArray
import org.json.JSONObject

class NinchatConversationQuestionnairePresenter(
        questionnaire: NinchatQuestionnaire? = null,
        botDetails: Pair<String?, String?>? = null,
        val viewCallback: INinchatConversationQuestionnaire,
) : INinchatQuestionnairePresenter {

    private val model = NinchatConversationQuestionnaireModel(
            questionnaire = questionnaire,
            botDetails = botDetails)

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