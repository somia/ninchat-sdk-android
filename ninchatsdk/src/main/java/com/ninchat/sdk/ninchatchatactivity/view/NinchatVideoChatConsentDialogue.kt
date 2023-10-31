package com.ninchat.sdk.ninchatchatactivity.view

import android.app.AlertDialog
import android.content.Context
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.adapters.NinchatMessageAdapter
import com.ninchat.sdk.helper.glidewrapper.GlideWrapper
import com.ninchat.sdk.utils.misc.NinchatAdapterCallback
import kotlinx.android.synthetic.main.dialog_video_call_consent.*

class NinchatVideoChatConsentDialogue {
    companion object {
        @JvmStatic
        fun show(
            context: Context, userId: String, messageId: String,
            onAccept: () -> Unit, onReject: () -> Unit
        ) {
            val dialog: AlertDialog =
                AlertDialog.Builder(context, R.style.NinchatTheme_Dialog)
                    .setView(R.layout.dialog_video_call_consent)
                    .setCancelable(false)
                    .create()
            dialog.show()
            NinchatSessionManager.getInstance()?.let { sessionManager ->
                dialog.ninchat_video_call_consent_dialog_title.text =
                    sessionManager.ninchatState.siteConfig.getVideoChatTitleText()

                val user = sessionManager.getMember(userId)
                val avatar = if (user?.avatar.isNullOrEmpty()) {
                    sessionManager.ninchatState.siteConfig.getAgentAvatar()
                } else {
                    user.avatar
                }
                if (!avatar.isNullOrEmpty()) {
                    GlideWrapper.loadImageAsCircle(
                        dialog.ninchat_video_call_consent_dialog_user_avatar.context,
                        avatar,
                        dialog.ninchat_video_call_consent_dialog_user_avatar
                    )
                }
                dialog.ninchat_video_call_consent_dialog_user_name.text = user?.name ?: ""
                dialog.ninchat_video_call_consent_dialog_description.text = sessionManager.ninchatState.siteConfig.getVideoChatDescriptionText() ?: ""

                // show accept button
                dialog.ninchat_video_call_consent_dialog_accept.let {
                    it.text = sessionManager.ninchatState.siteConfig.getVideoCallAcceptText()
                    it.setOnClickListener {
                        dialog.dismiss()
                        onAccept()
                    }
                }
                // show decline button
                dialog.ninchat_video_call_consent_dialog_decline.let {
                    it.text = sessionManager.ninchatState.siteConfig.getVideoCallDeclineText()
                    it.setOnClickListener {
                        dialog.dismiss()
                        onReject()
                    }
                }
                sessionManager.getOnInitializeMessageAdapter(
                    object : NinchatAdapterCallback {
                        override fun onMessageAdapter(adapter: NinchatMessageAdapter) {
                            adapter.addMetaMessage(
                                messageId,
                                sessionManager.ninchatState.siteConfig.getVideoCallMetaMessageText()
                            )
                        }
                    })
            }
        }
    }
}