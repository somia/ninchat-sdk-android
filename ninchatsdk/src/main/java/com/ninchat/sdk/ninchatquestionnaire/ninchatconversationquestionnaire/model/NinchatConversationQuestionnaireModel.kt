package com.ninchat.sdk.ninchatquestionnaire.ninchatconversationquestionnaire.model

import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire

data class NinchatConversationQuestionnaireModel(
        var questionnaire: NinchatQuestionnaire? = null,
        var botDetails: Pair<String?, String?>? = null,
        var isFormLikeQuestionnaire: Boolean = false
)