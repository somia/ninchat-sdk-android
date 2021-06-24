package com.ninchat.sdk.ninchattitlebar.model

import com.ninchat.sdk.NinchatSessionManager

/**
 * A data class that will contain all information related to title bar
 * https://github.com/somia/mobile/issues/343
 */

data class NinchatTitleBarInfo(
    val userAvatar: String? = null,
    val name: String? = null,
    val jobTitle: String? = null,
    val closeButtonText: String? = null,
    val showAvatar: Boolean = false
)

// Chat, rating view
fun getTitleBarInfoForChatAndRatings(): NinchatTitleBarInfo? {
    return NinchatSessionManager.getInstance()?.let { session ->
        val user = session.ninchatState?.members?.entries?.find { !it.value.isGuest }?.value
        val showAvatar = session.ninchatState?.siteConfig?.isFalse("agentAvatar") != true
        val userAvatar = when {
            session.ninchatState?.siteConfig?.isTrue("agentAvatar") == true -> user?.avatar
            else -> session.ninchatState?.siteConfig?.getAgentAvatar()
        } ?: "defaultIcon"

        val name = user?.name ?: session.ninchatState.siteConfig.getAgentName()
        val jobTitle = user?.jobTitle
        val closeButtonText = session.ninchatState?.siteConfig?.getTitlebarCloseText()
        NinchatTitleBarInfo(
            userAvatar = userAvatar,
            name = name,
            jobTitle = jobTitle,
            closeButtonText = closeButtonText,
            showAvatar = showAvatar
        )
    }

}

fun getTitleBarInfoForPreAudienceQuestionnaire(): NinchatTitleBarInfo? {
    return NinchatSessionManager.getInstance()?.let { session ->
        val name = session.ninchatState?.siteConfig?.getQuestionnaireName()
        val showAvatar =
            session.ninchatState?.siteConfig?.getQuestionnaireAvatar()?.let { true } ?: false
        val avatar = session.ninchatState?.siteConfig?.getQuestionnaireAvatar()
        val closeButtonText = session.ninchatState?.siteConfig?.getTitlebarCloseText()
        NinchatTitleBarInfo(
            name = name,
            userAvatar = avatar,
            closeButtonText = closeButtonText,
            showAvatar = showAvatar
        )
    }
}

fun getTitleBarInfoForPostAudienceQuestionnaire(): NinchatTitleBarInfo? {
    return NinchatSessionManager.getInstance()?.let { session ->
        val display = session.ninchatState?.siteConfig?.getPostQuestionnaireTitleBarDisplay()
            ?: ""
        return when (display) {
            "agent" -> {
                // similar to chat and rating -> show agent details
                getTitleBarInfoForChatAndRatings()
            }
            "questionnaire" -> {
                // similar to pre audience questionnaire -> show agent details
                getTitleBarInfoForPreAudienceQuestionnaire()
            }
            else -> {
                null
            }
        }
    }
}

fun getTitleBarInfoForQueue(): NinchatTitleBarInfo? {
    return NinchatSessionManager.getInstance()?.let { session ->
        val closeButtonText = session.ninchatState?.siteConfig?.getTitlebarCloseText()
        NinchatTitleBarInfo(closeButtonText = closeButtonText, showAvatar = true)
    }
}

fun chatCloseText(): String = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getTitlebarCloseText() ?: ""

fun shouldHideTitleBar(): Boolean {
    return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getHideTitleBar() ?: false
}