package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.model

import android.content.Intent
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireConstants
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireNormalizer
import org.json.JSONObject

data class NinchatQuestionnaireModel(
        var questionnaireType: Int = NinchatQuestionnaireConstants.preAudienceQuestionnaire,
        var queueId: String? = null,
        var isFormLike: Boolean = true,
        var questionnaireList: List<JSONObject> = listOf(),
) {

    fun update(intent: Intent?) {
        intent?.getIntExtra(QUESTIONNAIRE_TYPE, -1)?.let {
            questionnaireType = it
        }
        intent?.getStringExtra(QUEUE_ID)?.let {
            queueId = it
        }
        val questionnaireArr = if (questionnaireType == NinchatQuestionnaireConstants.preAudienceQuestionnaire)
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getPreAudienceQuestionnaire()
        else
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getPostAudienceQuestionnaire()

        questionnaireList = NinchatQuestionnaireNormalizer.unifyQuestionnaireList(questionnaireArr = questionnaireArr)

        isFormLike = if (questionnaireType == NinchatQuestionnaireConstants.preAudienceQuestionnaire) {
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getPreAudienceQuestionnaireStyle() != "conversation"
        } else {
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getPostAudienceQuestionnaireStyle() != "conversation"
        }
    }

    companion object {
        const val OPEN_QUEUE = "openQueue"
        const val QUEUE_ID = "queueId"
        const val QUESTIONNAIRE_TYPE = "questionType"
    }
}