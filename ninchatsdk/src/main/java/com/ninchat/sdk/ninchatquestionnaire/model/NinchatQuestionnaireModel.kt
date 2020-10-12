package com.ninchat.sdk.ninchatquestionnaire.model

import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.models.questionnaire.conversation.NinchatConversationQuestionnaire
import com.ninchat.sdk.models.questionnaire.form.NinchatFormQuestionnaire

class NinchatQuestionnaireModel {
    var queueId: String? = null
    var questionnaireType = POST_AUDIENCE_QUESTIONNAIRE
    var ninchatConversationQuestionnaire: NinchatConversationQuestionnaire? = null
    var ninchatFormQuestionnaire: NinchatFormQuestionnaire? = null

    fun getBotName(): String? = NinchatSessionManager.getInstance()?.ninchatState?.ninchatQuestionnaire?.botQuestionnaireName
    fun getBotAvatar(): String? = NinchatSessionManager.getInstance()?.ninchatState?.ninchatQuestionnaire?.botQuestionnaireAvatar

    fun isFormLikeQuestionnaire(questionnaireType: Int): Boolean {
        return !isConversationLikeQuestionnaire(questionnaireType = questionnaireType)
    }

    fun isConversationLikeQuestionnaire(questionnaireType: Int): Boolean {
        return if (questionnaireType == PRE_AUDIENCE_QUESTIONNAIRE) {
            NinchatSessionManager.getInstance().ninchatState?.ninchatQuestionnaire?.conversationLikePreAudienceQuestionnaire()
                    ?: false
        } else {
            NinchatSessionManager.getInstance().ninchatState?.ninchatQuestionnaire?.conversationLikePostAudienceQuestionnaire()
                    ?: false
        }
    }


    companion object {
        const val OPEN_QUEUE = "openQueue"
        const val QUEUE_ID = "queueId"
        const val QUESTIONNAIRE_TYPE = "questionType"
        const val PRE_AUDIENCE_QUESTIONNAIRE = 1
        const val POST_AUDIENCE_QUESTIONNAIRE = 2
    }
}