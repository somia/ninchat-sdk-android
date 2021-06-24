package com.ninchat.sdk.ninchattitlebar.view

import android.view.View
import com.ninchat.sdk.R
import com.ninchat.sdk.helper.glidewrapper.GlideWrapper
import com.ninchat.sdk.ninchattitlebar.model.NinchatTitleBarInfo
import com.ninchat.sdk.ninchattitlebar.model.chatCloseText
import com.ninchat.sdk.ninchattitlebar.model.getTitleBarInfoForPreAudienceQuestionnaire
import com.ninchat.sdk.ninchattitlebar.model.shouldHideTitleBar
import kotlinx.android.synthetic.main.ninchat_titlebar.view.*
import kotlinx.android.synthetic.main.ninchat_titlebar_with_agent_info.view.*


class NinchatTitlebarView {
    companion object {
        private fun showTitlebarPlaceholder(view: View, callback: () -> Unit) {
            view.ninchat_titlebar_with_placeholder.visibility = View.VISIBLE
            view.ninchat_titlebar_with_agent_info.visibility = View.GONE
            view.ninchat_titlebar_with_agent_info_and_job_title.visibility = View.GONE
            view.ninchat_titlebar_chat_close.text = chatCloseText()
            view.visibility = View.VISIBLE
            view.ninchat_titlebar_chat_close?.let {
                it.setOnClickListener { callback() }
                it.text = chatCloseText()
            }
        }

        private fun showTitlebarWithAgentInfo(
            view: View,
            data: NinchatTitleBarInfo,
            callback: () -> Unit
        ) {
            // if questionnaire name -> show questionnaire name
            if(data.hasName) {
                view.ninchat_chat_titlebar_agent_name.text = data.name
            }
            if(data.hasAvatar) {
                GlideWrapper.loadImageAsCircle(view.context, data.userAvatar, view.ninchat_chat_titlebar_avatar_img, R.drawable.ninchat_chat_avatar_left)
            } else {
                // otherwise hide avatar
                view.ninchat_chat_titlebar_avatar_img.visibility = View.GONE
            }
            // if questionnaire avatar -> show questionnaire avatar
            view.ninchat_titlebar_with_agent_info.visibility = View.VISIBLE
            view.ninchat_titlebar_with_placeholder.visibility = View.GONE
            view.ninchat_titlebar_with_agent_info_and_job_title.visibility = View.GONE
            view.ninchat_titlebar_chat_close.text = chatCloseText()
            view.visibility = View.VISIBLE
            view.ninchat_titlebar_chat_close?.let {
                it.setOnClickListener { callback() }
                it.text = chatCloseText()
            }
        }

        private fun showTitlebarWithAgentInfoAndJobTitle(
            view: View,
            data: NinchatTitleBarInfo,
            callback: () -> Unit
        ) {

        }

        fun showTitlebarForPreAudienceQuestionnaire(view: View, callback: () -> Unit) {
            val titleBarInfo = getTitleBarInfoForPreAudienceQuestionnaire() ?: return

            //1: if no questionnaire name
            //2: if no questionnaire name and no questionnaire avatar
            if (!titleBarInfo.hasName || (!titleBarInfo.hasName && !titleBarInfo.hasAvatar)) {
                // show placeholder
                showTitlebarPlaceholder(view, callback = callback)
                return
            }
            showTitlebarWithAgentInfo(view = view, data = titleBarInfo, callback = callback)
        }

        fun showTitlebarForInQueueView(view: View, callback: () -> Unit) {
            if (shouldHideTitleBar()) return
            showTitlebarPlaceholder(view, callback = callback)
        }
    }

}