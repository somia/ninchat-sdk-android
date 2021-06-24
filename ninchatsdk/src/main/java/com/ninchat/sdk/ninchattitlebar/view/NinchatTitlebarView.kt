package com.ninchat.sdk.ninchattitlebar.view

import android.view.View
import com.ninchat.sdk.ninchattitlebar.model.NinchatTitleBarInfo
import com.ninchat.sdk.ninchattitlebar.model.chatCloseText
import com.ninchat.sdk.ninchattitlebar.model.shouldHideTitleBar
import kotlinx.android.synthetic.main.ninchat_titlebar.view.*


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

        private fun showTitlebarWithAgentInfo(view: View, data: NinchatTitleBarInfo, callback: () -> Unit) {

        }

        private fun showTitlebarWithAgentInfoAndJobTitle(
            view: View,
            data: NinchatTitleBarInfo,
            callback: () -> Unit
        ) {

        }

        fun showTitlebarForPreAudienceQuestionnaire(view: View, callback: () -> Unit) {

        }

        fun showTitlebarForInQueueView(view: View, callback: () -> Unit) {
            if(shouldHideTitleBar())return
            showTitlebarPlaceholder(view, callback = callback)
        }
    }

}