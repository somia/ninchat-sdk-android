package com.ninchat.sdk.models

import com.ninchat.sdk.NinchatSessionManager

/**
 * A data class that will contain all information related to title bar
 * https://github.com/somia/mobile/issues/343
 */

data class NinchatTitlebarInfo(
    val avatar: String? = null,
    val name: String? = null,
    val jobTitle: String? = null,
    val closeButtonText: String? = null
)

// Chat, ratting view
fun getTitlebarInfoForChatAndRatings(): NinchatTitlebarInfo? {
    return NinchatSessionManager.getInstance()?.let { session ->
        val user = session.ninchatState?.members?.entries?.find { !it.value.isGuest }?.value
        val avatar = user?.avatar ?: session.ninchatState.siteConfig.getAgentAvatar()
        val name = user?.name ?: session.ninchatState.siteConfig.getAgentName()
        val jobTitle = user?.jobTitle
        val closeButtonText = session.ninchatState?.siteConfig?.getTitlebarCloseText()
        NinchatTitlebarInfo(
            avatar = avatar,
            name = name,
            jobTitle = jobTitle,
            closeButtonText = closeButtonText
        )
    }

}

fun getTitlebarInfoForQuestionnaire(): NinchatTitlebarInfo? {
    return NinchatSessionManager.getInstance()?.let { session ->
        val name = session.ninchatState?.siteConfig?.getQuestionnaireName()
        val avatar = session.ninchatState?.siteConfig?.getQuestionnaireAvatar()
        val closeButtonText = session.ninchatState?.siteConfig?.getTitlebarCloseText()
        NinchatTitlebarInfo(name = name, avatar = avatar, closeButtonText = closeButtonText)
    }
}

fun getTitlebarInfoForQueue(): NinchatTitlebarInfo? {
    return NinchatSessionManager.getInstance()?.let { session ->
        val closeButtonText = session.ninchatState?.siteConfig?.getTitlebarCloseText()
        NinchatTitlebarInfo(closeButtonText = closeButtonText)
    }
}

fun shouldHideTitlebar(): Boolean {
    return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getHideTitleBar() ?: true
}