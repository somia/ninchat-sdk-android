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
    val hasAvatar: Boolean = false,
    val hasName: Boolean = false,
    val hasJobTitle: Boolean = false,

    )

// Chat, rating view
fun getTitleBarInfoFromAgent(): NinchatTitleBarInfo? {
    return NinchatSessionManager.getInstance()?.let { session ->
        val user = session.ninchatState?.members?.entries?.find { !it.value.isGuest }?.value
        val name = user?.name ?: session.ninchatState.siteConfig.getAgentName()
        val hasName = !name.isNullOrEmpty()
        val jobTitle = user?.jobTitle
        val hasJobTitle = !jobTitle.isNullOrEmpty()

        val userAvatar = when {
            // is agent avatar is true -> use user avatar
            session.ninchatState?.siteConfig?.isTrue("agentAvatar") == true -> user?.avatar
            else -> session.ninchatState?.siteConfig?.getAgentAvatar()
        }
        // is not strictly false
        val hasAvatar = session.ninchatState?.siteConfig?.isFalse("agentAvatar") == false
        val closeButtonText = session.ninchatState?.siteConfig?.getTitlebarCloseText()
        NinchatTitleBarInfo(
            name = name,
            hasName = hasName,
            jobTitle = jobTitle,
            hasJobTitle = hasJobTitle,
            userAvatar = userAvatar,
            hasAvatar = hasAvatar,
            closeButtonText = closeButtonText,
        )
    }

}

fun getTitleBarInfoFromAudienceQuestionnaire(): NinchatTitleBarInfo? {
    return NinchatSessionManager.getInstance()?.let { session ->
        val name = session.ninchatState?.siteConfig?.getQuestionnaireName()
        val hasName = !name.isNullOrEmpty()
        val avatar = session.ninchatState?.siteConfig?.getQuestionnaireAvatar()
        val hasAvatar = session.ninchatState?.siteConfig?.hasValue("questionnaireAvatar") ?: false
        val closeButtonText = session.ninchatState?.siteConfig?.getTitlebarCloseText()
        NinchatTitleBarInfo(
            name = name,
            hasName = hasName,
            userAvatar = avatar,
            hasAvatar = hasAvatar,
            closeButtonText = closeButtonText,
        )
    }
}

fun titlebarDisplayInfo(): String =
    NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getPostQuestionnaireTitleBarDisplay()
        ?: ""

fun chatCloseText(): String =
    NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getTitlebarCloseText() ?: ""

// hideTitlebar == false -> show titlebar
// hideTitlebar == true -> don't show titlebar
// no attribute name as hideTitlebar -> don't show titlebar
fun shouldShowTitlebar(): Boolean {
    return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.let {
        it.isFalse("hideTitlebar") && it.hasValue("hideTitlebar")
    } ?: false
}