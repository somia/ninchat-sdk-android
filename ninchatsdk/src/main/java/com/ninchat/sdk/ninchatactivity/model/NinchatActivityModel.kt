package com.ninchat.sdk.ninchatactivity.model

import com.ninchat.sdk.NinchatSessionManager

class NinchatActivityModel {
    var queueId: String? = null

    fun getWelcomeMessage(): String =
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getWelcomeText() ?: ""

    fun getNoQueueText(): String =
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getNoQueuesText() ?: ""

    fun getMotD(): String =
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getMOTDText() ?: ""

    fun getCloseWindowText(): String =
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getCloseWindowText()
                    ?: ""

    companion object {
        const val QUEUE_ID = "queueId"
        const val TRANSITION_DELAY = 300L
    }
}