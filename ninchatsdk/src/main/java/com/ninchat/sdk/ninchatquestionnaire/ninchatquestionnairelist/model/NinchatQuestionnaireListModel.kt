package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.model

import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireConstants
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireNormalizer
import org.json.JSONObject

data class NinchatQuestionnaireListModel(
        val questionnaireType: Int,
        var questionnaireList: List<JSONObject> = listOf(),
) {

    fun parse() {
        val questionnaireArr = if (questionnaireType == NinchatQuestionnaireConstants.preAudienceQuestionnaire)
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getPreAudienceQuestionnaire()
        else
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getPostAudienceQuestionnaire()
        questionnaireList = NinchatQuestionnaireNormalizer.unifyQuestionnaireList(questionnaireArr = questionnaireArr)
    }

    fun update() {

    }
}