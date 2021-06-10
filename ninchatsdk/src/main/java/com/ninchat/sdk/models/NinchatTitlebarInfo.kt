package com.ninchat.sdk.models

import com.ninchat.sdk.NinchatSessionManager

/**
 * A data class that will contain all information related to title bar
 * https://github.com/somia/mobile/issues/343
 */

data class NinchatTitlebarInfo(
    val userAvatar: String? = null,
    val name: String? = null,
    val jobTitle: String? = null,
    val closeButtonText: String? = null,
    val showAvatar: Boolean? = false
)

// Chat, rating view
fun getTitlebarInfoForChatAndRatings(): NinchatTitlebarInfo? {
    return NinchatSessionManager.getInstance()?.let { session ->
        val user = session.ninchatState?.members?.entries?.find { !it.value.isGuest }?.value
        val userAvatar = user?.avatar
        val name = user?.name ?: session.ninchatState.siteConfig.getAgentName()
        val jobTitle = user?.jobTitle
        val closeButtonText = session.ninchatState?.siteConfig?.getTitlebarCloseText()
        val showAvatar = session.ninchatState.siteConfig.showAgentAvatar(fallback = true)
        NinchatTitlebarInfo(
            userAvatar=userAvatar,
            name = name,
            jobTitle = jobTitle,
            closeButtonText = closeButtonText,
            showAvatar = showAvatar
        )
    }

}

fun getTitlebarInfoForQuestionnaire(): NinchatTitlebarInfo? {
    return NinchatSessionManager.getInstance()?.let { session ->
        val name = session.ninchatState?.siteConfig?.getQuestionnaireName()
        val avatar = session.ninchatState?.siteConfig?.getQuestionnaireAvatar()
        val closeButtonText = session.ninchatState?.siteConfig?.getTitlebarCloseText()
        val showAvatar = session.ninchatState.siteConfig.showAgentAvatar(fallback = true)
        NinchatTitlebarInfo(name = name, userAvatar = avatar, closeButtonText = closeButtonText, showAvatar = showAvatar)
    }
}

fun getTitlebarInfoForQueue(): NinchatTitlebarInfo? {
    return NinchatSessionManager.getInstance()?.let { session ->
        val closeButtonText = session.ninchatState?.siteConfig?.getTitlebarCloseText()
        val showAvatar = session.ninchatState.siteConfig.showAgentAvatar(fallback = true)
        NinchatTitlebarInfo(closeButtonText = closeButtonText, showAvatar = showAvatar)
    }
}

fun shouldHideTitlebar(): Boolean {
    return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getHideTitleBar() ?: true
}