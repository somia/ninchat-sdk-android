package com.ninchat.sdk.ninchattitlebar.view

import android.view.View
import com.ninchat.sdk.R
import com.ninchat.sdk.helper.glidewrapper.GlideWrapper
import com.ninchat.sdk.ninchattitlebar.model.*
import kotlinx.android.synthetic.main.ninchat_titlebar.view.*
import kotlinx.android.synthetic.main.ninchat_titlebar_with_agent_info.view.*


class NinchatTitlebarView {
    companion object {
        private fun showTitlebarPlaceholder(
            view: View,
            inBacklogView: Boolean = false,
            callback: () -> Unit,
            onToggleChat: (() -> Unit)? = null
        ) {
            if (!inBacklogView && !shouldShowTitlebar()) return
            if (inBacklogView && !showOverrideTitlebarView()) return

            view.ninchat_titlebar_with_placeholder.visibility = View.VISIBLE
            view.ninchat_titlebar_with_agent_info.visibility = View.GONE
            view.ninchat_titlebar_chat_close.text = chatCloseText()
            view.visibility = View.VISIBLE
            view.ninchat_titlebar_chat_close?.let {
                it.setOnClickListener { callback() }
                it.text = chatCloseText()
            }
            view.ninchat_titlebar_toggle_chat?.let {
                it.setOnClickListener { onToggleChat?.invoke() }
            }
        }

        private fun showTitlebarWithAgentInfo(
            view: View,
            inBacklogView: Boolean = false,
            data: NinchatTitleBarInfo,
            callback: () -> Unit,
            onToggleChat: (() -> Unit)? = null
        ) {
            if (!inBacklogView && !shouldShowTitlebar()) return
            if (inBacklogView && !showOverrideTitlebarView()) return

            // if questionnaire name -> show questionnaire name
            if (data.hasName) {
                view.ninchat_chat_titlebar_agent_name.text = data.name
            }
            if (data.hasAvatar) {
                GlideWrapper.loadImageAsCircle(
                    view.context,
                    data.userAvatar,
                    view.ninchat_chat_titlebar_avatar_img,
                    R.drawable.ninchat_chat_avatar_left
                )
            } else {
                // otherwise hide avatar
                view.ninchat_chat_titlebar_avatar_img.visibility = View.GONE
            }
            if (data.hasJobTitle) {
                view.ninchat_chat_titlebar_agent_jobtitle.text = data.jobTitle
            } else {
                // otherwise hide job title
                view.ninchat_chat_titlebar_agent_jobtitle.visibility = View.GONE
            }
            // if questionnaire avatar -> show questionnaire avatar
            view.ninchat_titlebar_with_agent_info.visibility = View.VISIBLE
            view.ninchat_titlebar_with_placeholder.visibility = View.GONE
            view.ninchat_titlebar_chat_close.text = chatCloseText()
            view.visibility = View.VISIBLE
            view.ninchat_titlebar_chat_close?.let {
                it.setOnClickListener { callback() }
                it.text = chatCloseText()
            }
            view.ninchat_titlebar_toggle_chat?.let {
                it.setOnClickListener { onToggleChat?.invoke() }
            }
        }

        fun showTitlebarForPreAudienceQuestionnaire(view: View, callback: () -> Unit) {
            val titleBarInfo = getTitleBarInfoFromAudienceQuestionnaire() ?: return
            //1: if no questionnaire name
            //2: if no questionnaire name and no questionnaire avatar
            if (!titleBarInfo.hasName || (!titleBarInfo.hasName && !titleBarInfo.hasAvatar)) {
                // show placeholder
                showTitlebarPlaceholder(view, callback = callback)
                return
            }
            showTitlebarWithAgentInfo(view = view, data = titleBarInfo, callback = callback)
        }

        fun showTitlebarForPostAudienceQuestionnaire(view: View, callback: () -> Unit) {
            val displayInfo = titlebarDisplayInfo()
            val titleBarInfo = when (displayInfo) {
                "questionnaire" -> getTitleBarInfoFromAudienceQuestionnaire()
                "agent" -> getTitleBarInfoFromAgent()
                else -> getTitleBarInfoFromAudienceQuestionnaire()
            } ?: return
            //1: if no questionnaire name
            //2: if no questionnaire name and no questionnaire avatar
            if (!titleBarInfo.hasName || (!titleBarInfo.hasName && !titleBarInfo.hasAvatar)) {
                // show placeholder
                showTitlebarPlaceholder(view, callback = callback)
                return
            }
            showTitlebarWithAgentInfo(view = view, data = titleBarInfo, callback = callback)
        }

        fun showTitlebarForBacklog(view: View, callback: () -> Unit, onToggleChat: () -> Unit) {
            val titleBarInfo = getTitleBarInfoFromAgent() ?: return
            //1: if no name
            //2: if no name and no avatar
            if (!titleBarInfo.hasName || (!titleBarInfo.hasName && !titleBarInfo.hasAvatar)) {
                // show placeholder
                showTitlebarPlaceholder(view, callback = callback, onToggleChat = onToggleChat)
                return
            }
            showTitlebarWithAgentInfo(view = view, data = titleBarInfo, callback = callback, onToggleChat = onToggleChat)
        }

        fun showTitlebarForReview(view: View, callback: () -> Unit) {
            val titleBarInfo = getTitleBarInfoFromAgent() ?: return
            //1: if no name
            //2: if no name and no avatar
            if (!titleBarInfo.hasName || (!titleBarInfo.hasName && !titleBarInfo.hasAvatar)) {
                // show placeholder
                showTitlebarPlaceholder(view, callback = callback)
                return
            }
            showTitlebarWithAgentInfo(view = view, data = titleBarInfo, callback = callback)
        }

        fun showTitlebarForInQueueView(view: View, callback: () -> Unit) {
            showTitlebarPlaceholder(view, callback = callback)
        }
    }

}