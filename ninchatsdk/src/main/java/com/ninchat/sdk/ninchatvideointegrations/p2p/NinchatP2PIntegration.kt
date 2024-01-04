package com.ninchat.sdk.ninchatvideointegrations.p2p

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.view.Surface
import android.view.View
import android.widget.LinearLayout
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.adapters.NinchatMessageAdapter
import com.ninchat.sdk.networkdispatchers.NinchatSendMessage
import com.ninchat.sdk.ninchatchatactivity.view.NinchatChatActivity
import com.ninchat.sdk.ninchatchatactivity.view.NinchatVideoChatConsentDialogue
import com.ninchat.sdk.ninchattitlebar.model.shouldShowTitlebar
import com.ninchat.sdk.utils.keyboard.hideKeyBoardForce
import com.ninchat.sdk.utils.messagetype.NinchatMessageTypes
import com.ninchat.sdk.utils.misc.Broadcast
import com.ninchat.sdk.utils.misc.Misc.Companion.center
import com.ninchat.sdk.utils.misc.NinchatAdapterCallback
import com.ninchat.sdk.utils.permission.NinchatPermission.Companion.hasVideoCallPermissions
import com.ninchat.sdk.utils.permission.NinchatPermission.Companion.requestAudioVideoPermissions
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
import com.ninchat.sdk.views.NinchatWebRTCView
import kotlinx.android.synthetic.main.activity_ninchat_chat.*
import kotlinx.android.synthetic.main.activity_ninchat_chat.view.*
import kotlinx.android.synthetic.main.ninchat_p2p_video_container.view.*
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class NinchatP2PIntegration(
    private val videoContainer: View,
    private val mActivity: NinchatChatActivity,
) {
    private val webRTCView: NinchatWebRTCView = NinchatWebRTCView(videoContainer)

    init {
        renderView(activeCall = false)
    }

    private fun renderView(activeCall: Boolean = false) {
        mActivity.ninchat_chat_root?.apply {
            chat_message_list_and_editor.layoutParams =
                chat_message_list_and_editor.layoutParams.let {
                    val layoutParams = it as LinearLayout.LayoutParams
                    layoutParams.weight = if (activeCall) 2.1f else 3f
                    layoutParams
                }
            conference_or_p2p_view_container.layoutParams =
                conference_or_p2p_view_container.layoutParams.let {
                    val layoutParams = it as LinearLayout.LayoutParams
                    layoutParams.weight = if (activeCall) 0.9f else 0f
                    layoutParams
                }
        }
    }

    fun onSoftKeyboardVisibilityChanged(isVisible: Boolean) {
        if (!webRTCView.isInCall) {
            return
        }
        mActivity.ninchat_chat_root?.apply {
            chat_message_list_and_editor.layoutParams =
                chat_message_list_and_editor.layoutParams.let {
                    val layoutParams = it as LinearLayout.LayoutParams
                    layoutParams.weight = 2.1f
                    layoutParams
                }
            conference_or_p2p_view_container.layoutParams =
                conference_or_p2p_view_container.layoutParams.let {
                    val layoutParams = it as LinearLayout.LayoutParams
                    layoutParams.weight = 0.9f
                    layoutParams
                }
        }
        return
    }

    fun mayBeHandleWebRTCMessages(intent: Intent, activity: Activity) {
        val messageType = intent.getStringExtra(Broadcast.WEBRTC_MESSAGE_TYPE)
        val payload = intent.getStringExtra(Broadcast.WEBRTC_MESSAGE_CONTENT)
        if (webRTCView.handleWebRTCMessage(messageType, payload)) {
            if (NinchatMessageTypes.HANG_UP == messageType) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
                renderView(activeCall = false)
                handleTitlebarView(true, activity)
            }
        }
    }

    fun maybeHandleP2PVideoCallInvitation(
        intent: Intent,
        activity: Activity,
    ) {
        if (NinchatMessageTypes.CALL != intent.getStringExtra(Broadcast.WEBRTC_MESSAGE_TYPE)) {
            return
        }
        videoContainer.hideKeyBoardForce();
        NinchatVideoChatConsentDialogue.show(
            context = videoContainer.context,
            userId = intent.getStringExtra(Broadcast.WEBRTC_MESSAGE_SENDER) ?: "",
            messageId = intent.getStringExtra(Broadcast.WEBRTC_MESSAGE_ID) ?: "",
            onAccept = {
                if (hasVideoCallPermissions(videoContainer.context)) {
                    sendPickUpAnswer(true)
                } else {
                    requestAudioVideoPermissions(mActivity)
                }
            },
            onReject = {
                sendPickUpAnswer(false)
            },
        )
    }


    fun sendPickUpAnswer(answer: Boolean) {
        renderView(activeCall = answer)
        NinchatSessionManager.getInstance()?.let { sessionManager ->
            try {
                val data = JSONObject().apply {
                    putOpt("answer", answer)
                }
                NinchatScopeHandler.getIOScope().launch {
                    NinchatSendMessage.execute(
                        currentSession = sessionManager.session,
                        channelId = sessionManager.ninchatState.channelId,
                        message = data.toString(),
                        messageType = NinchatMessageTypes.PICK_UP,
                    )
                }
            } catch (e: JSONException) {
                sessionManager.sessionError(e)
            }
            val metaMessage =
                if (answer) sessionManager.ninchatState.siteConfig.getVideoCallAcceptedText() else sessionManager.ninchatState.siteConfig.getVideoCallRejectedText()
            sessionManager.getOnInitializeMessageAdapter(object : NinchatAdapterCallback {
                override fun onMessageAdapter(adapter: NinchatMessageAdapter) {
                    adapter.addMetaMessage(
                        adapter.getLastMessageId(true) + "answer",
                        center(metaMessage)
                    )
                }
            })
        }
    }


    fun handleTitlebarView(pendingHangup: Boolean, activity: Activity) {
        if (!shouldShowTitlebar()) return
        val inActiveVideoCall = isInCall() && !pendingHangup

        when (activity.windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                activity.ninchat_chat_root.ninchat_titlebar.visibility = View.VISIBLE
                return
            }
            Surface.ROTATION_270, Surface.ROTATION_90 -> {
                activity.ninchat_chat_root.ninchat_titlebar.visibility =
                    if (inActiveVideoCall) View.GONE else View.VISIBLE
                return
            }
        }
    }


    fun handleOrientationChange(
        currentOrientation: Int,
        pendingHangup: Boolean,
    ) {
        handleTitlebarView(pendingHangup = pendingHangup, activity = mActivity)
        if (!webRTCView.isInCall) {
            return
        }
        if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            mActivity.ninchat_chat_root?.apply {
                conference_or_p2p_view_container.layoutParams =
                    conference_or_p2p_view_container.layoutParams.let {
                        val layoutParams = it as LinearLayout.LayoutParams
                        layoutParams.weight = 0.9f
                        layoutParams
                    }
                chat_message_list_and_editor.layoutParams =
                    chat_message_list_and_editor.layoutParams.let {
                        val layoutParams = it as LinearLayout.LayoutParams
                        layoutParams.weight = 2.1f
                        layoutParams
                    }
                fullscreen_on_off.apply {
                    setImageResource(R.drawable.ninchat_icon_video_toggle_full)
                }
            }
        } else if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mActivity.ninchat_p2p_video_view.layoutParams.apply {
                height = LinearLayout.LayoutParams.MATCH_PARENT
            }
            mActivity.ninchat_chat_root?.apply {
                conference_or_p2p_view_container.layoutParams =
                    conference_or_p2p_view_container.layoutParams.let {
                        val layoutParams = it as LinearLayout.LayoutParams
                        layoutParams.weight = 3f
                        layoutParams
                    }
                chat_message_list_and_editor.layoutParams =
                    chat_message_list_and_editor.layoutParams.let {
                        val layoutParams = it as LinearLayout.LayoutParams
                        layoutParams.weight = 0f
                        layoutParams
                    }
                fullscreen_on_off.apply {
                    setImageResource(R.drawable.ninchat_icon_video_toggle_normal)
                }
            }
        }
    }

    fun call() {
        renderView(activeCall = true)
        webRTCView.call()
    }

    private fun isInCall() = webRTCView.isInCall

    fun hangUp() {
        // render view
        renderView(activeCall = false)
        webRTCView.hangUp()
    }

    fun toggleAudio() {
        webRTCView.toggleAudio()
    }

    fun toggleMicrophone() {
        webRTCView.toggleMicrophone()
    }

    fun toggleVideo() {
        webRTCView.toggleVideo()
    }

    fun onChannelClosed() {
        this.hangUp()
    }
}



