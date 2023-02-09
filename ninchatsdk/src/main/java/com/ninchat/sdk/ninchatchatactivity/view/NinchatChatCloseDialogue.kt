package com.ninchat.sdk.ninchatchatactivity.view

import android.app.AlertDialog
import android.content.Context
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import kotlinx.android.synthetic.main.dialog_close_chat.*

class NinchatChatCloseDialogue {

    companion object {
        fun show(context: Context, isChatClosed: Boolean, onAccept: () -> Unit) {
            val dialog: AlertDialog = AlertDialog.Builder(context, R.style.NinchatTheme_Dialog)
                .setView(R.layout.dialog_close_chat)
                .setCancelable(true)
                .create()
            dialog.show()
            NinchatSessionManager.getInstance()?.let { sessionManager ->
                dialog.ninchat_close_chat_dialog_title.text =
                    sessionManager.ninchatState.siteConfig.getChatCloseText()
                dialog.ninchat_close_chat_dialog_description.text =
                    sessionManager.ninchatState.siteConfig.getChatCloseConfirmationText()

                dialog.ninchat_close_chat_dialog_confirm.let {
                    it.text = sessionManager.ninchatState.siteConfig.getChatCloseText()
                    it.setOnClickListener {
                        dialog.dismiss()
                        onAccept()
                    }
                }
                dialog.ninchat_close_chat_dialog_decline.let {
                    it.text = sessionManager.ninchatState.siteConfig.getContinueChatText()
                    it.setOnClickListener {
                        dialog.dismiss()
                    }
                }
                if (isChatClosed) {
                    dialog.dismiss()
                    // consider as auto accept
                    onAccept()
                }
            }
        }
    }
}

