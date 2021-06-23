package com.ninchat.sdk.titlebar.view

import android.view.View
import com.ninchat.sdk.titlebar.model.chatCloseText
import kotlinx.android.synthetic.main.ninchat_titlebar.view.*


class NinchatTitlebarView {
    companion object {
        fun showTitlebarPlaceholder(view: View, callback: () -> Unit) {
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
    }

}