package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.model

import android.content.Intent
import com.ninchat.sdk.NinchatSessionManager

data class NinchatQuestionnaireModel(
        var questionnaireType: Int = preAudienceQuestionnaire,
        var queueId: String? = null,
) {

    fun update(intent: Intent?) {
        intent?.getIntExtra(QUESTIONNAIRE_TYPE, -1)?.let {
            questionnaireType = it
        }
        intent?.getStringExtra(QUEUE_ID)?.let {
            queueId = it
        }
    }

    companion object {
        const val OPEN_QUEUE = "openQueue"
        const val QUEUE_ID = "queueId"
        const val QUESTIONNAIRE_TYPE = "questionType"
        const val preAudienceQuestionnaire = 1
        const val postAudienceQuestionnaire = 2
    }
}