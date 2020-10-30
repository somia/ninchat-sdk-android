package com.ninchat.sdk.ninchatquestionnaire.ninchatformquestionnaire.model

import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire

data class NinchatFormQuestionnaireModel(
        var questionnaire: NinchatQuestionnaire? = null,
        var botDetails: Pair<String, String>? = null,
        var isFormLikeQuestionnaire: Boolean = true,
)