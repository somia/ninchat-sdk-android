package com.ninchat.sdk.models

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
    val showAvatar: Boolean? = false
)

// Chat, rating view
fun getTitleBarInfoForChatAndRatings(): NinchatTitleBarInfo? {
    return NinchatSessionManager.getInstance()?.let { session ->
        val user = session.ninchatState?.members?.entries?.find { !it.value.isGuest }?.value
        val userAvatar = user?.avatar
        val name = user?.name ?: session.ninchatState.siteConfig.getAgentName()
        val jobTitle = user?.jobTitle
        val closeButtonText = session.ninchatState?.siteConfig?.getTitlebarCloseText()
        val showAvatar = session.ninchatState.siteConfig.showAgentAvatar(fallback = true)
        NinchatTitleBarInfo(
            userAvatar=userAvatar,
            name = name,
            jobTitle = jobTitle,
            closeButtonText = closeButtonText,
            showAvatar = showAvatar
        )
    }

}

fun getTitleBarInfoForQuestionnaire(): NinchatTitleBarInfo? {
    return NinchatSessionManager.getInstance()?.let { session ->
        val name = session.ninchatState?.siteConfig?.getQuestionnaireName()
        val avatar = session.ninchatState?.siteConfig?.getQuestionnaireAvatar()
        val closeButtonText = session.ninchatState?.siteConfig?.getTitlebarCloseText()
        val showAvatar = session.ninchatState.siteConfig.showAgentAvatar(fallback = true)
        NinchatTitleBarInfo(name = name, userAvatar = avatar, closeButtonText = closeButtonText, showAvatar = showAvatar)
    }
}

fun getTitleBarInfoForQueue(): NinchatTitleBarInfo? {
    return NinchatSessionManager.getInstance()?.let { session ->
        val closeButtonText = session.ninchatState?.siteConfig?.getTitlebarCloseText()
        NinchatTitleBarInfo(closeButtonText = closeButtonText, showAvatar = true)
    }
}

fun shouldHideTitleBar(): Boolean {
    return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getHideTitleBar() ?: true
}