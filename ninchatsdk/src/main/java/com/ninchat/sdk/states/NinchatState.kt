package com.ninchat.sdk.states

import com.ninchat.client.Props
import com.ninchat.sdk.helper.siteconfigparser.NinchatSiteConfig
import com.ninchat.sdk.models.NinchatSessionCredentials
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaireHolder

object NinchatState {
    var userId: String? = null
    var queueId: String? = null
    var currentSessionState: Int = 0

    var userChannels: Props? = null
    var userQueues: Props? = null

    var sessionCredentials: NinchatSessionCredentials? = null

    var openQueueList: List<String>? = null

    var siteConfig: NinchatSiteConfig? = null
    var ninchatQuestionnaire: NinchatQuestionnaireHolder? = null

    @JvmStatic
    fun reset() {
        userId = null
        queueId = null
        currentSessionState = 0

        userChannels = null
        userQueues = null
        sessionCredentials = null

        openQueueList = null

        siteConfig = null
        ninchatQuestionnaire = null
    }
}