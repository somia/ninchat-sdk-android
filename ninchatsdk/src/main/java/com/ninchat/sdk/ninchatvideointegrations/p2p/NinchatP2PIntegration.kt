package com.ninchat.sdk.ninchatvideointegrations.p2p

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.PermissionChecker.checkCallingOrSelfPermission
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.adapters.NinchatMessageAdapter
import com.ninchat.sdk.networkdispatchers.NinchatSendMessage
import com.ninchat.sdk.ninchatchatactivity.presenter.NinchatChatPresenter
import com.ninchat.sdk.ninchatchatactivity.view.NinchatChatActivity
import com.ninchat.sdk.ninchatchatactivity.view.NinchatVideoChatConsentDialogue
import com.ninchat.sdk.ninchattitlebar.model.shouldShowTitlebar
import com.ninchat.sdk.utils.keyboard.hideKeyBoardForce
import com.ninchat.sdk.utils.messagetype.NinchatMessageTypes
import com.ninchat.sdk.utils.misc.Broadcast
import com.ninchat.sdk.utils.misc.Misc.Companion.center
import com.ninchat.sdk.utils.misc.NinchatAdapterCallback
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

    fun onConfigurationChanges(newConfig: Configuration) {
        videoContainer.fullscreen_on_off?.apply {
            setImageResource(
                when (newConfig.orientation) {
                    Configuration.ORIENTATION_LANDSCAPE -> R.drawable.ninchat_icon_video_toggle_normal
                    else -> R.drawable.ninchat_icon_video_toggle_full
                }
            )
        }
        videoContainer.layoutParams?.apply {
            height = when (newConfig.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> ViewGroup.LayoutParams.MATCH_PARENT
                else -> videoContainer.resources.getDimensionPixelSize(R.dimen.ninchat_chat_activity_video_view_height)
            }
        }
        videoContainer.pip_video.layoutParams?.apply {
            width =
                videoContainer.resources.getDimensionPixelSize(R.dimen.ninchat_chat_activity_pip_video_width)
            height =
                videoContainer.resources.getDimensionPixelSize(R.dimen.ninchat_chat_activity_pip_video_height)
        }

        NinchatSessionManager.getInstance()?.getOnInitializeMessageAdapter(
            object : NinchatAdapterCallback {
                override fun onMessageAdapter(adapter: NinchatMessageAdapter) {
                    adapter.scrollToBottom(true)
                }
            })

    }

    fun onSoftKeyboardVisibilityChanged(isVisible: Boolean) {
        mActivity.findViewById<RelativeLayout>(R.id.ninchat_chat_root)?.apply {
            ninchat_p2p_video_view.layoutParams.height = if( isVisible ) mActivity.resources.getDimension(R.dimen.ninchat_chat_activity_video_view_height_small).toInt() else mActivity.resources.getDimension(R.dimen.ninchat_chat_activity_video_view_height).toInt()
            ninchat_p2p_video_view.requestLayout()
        }
    }

    fun mayBeHandleWebRTCMessages(intent: Intent, activity: Activity) {
        val messageType = intent.getStringExtra(Broadcast.WEBRTC_MESSAGE_TYPE)
        val payload = intent.getStringExtra(Broadcast.WEBRTC_MESSAGE_CONTENT)
        if (webRTCView.handleWebRTCMessage(messageType, payload)) {
            if (NinchatMessageTypes.HANG_UP == messageType) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
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
                if (hasVideoCallPermissions()) {
                    sendPickUpAnswer(true)
                } else {
                    requestPermissions(
                        activity,
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                        ),
                        NinchatChatPresenter.CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE,
                    )
                }
            },
            onReject = {
                sendPickUpAnswer(false)
            },
        )
    }

    fun hasVideoCallPermissions(): Boolean {
        return checkCallingOrSelfPermission(
            videoContainer.context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED &&
                checkCallingOrSelfPermission(
                    videoContainer.context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
    }


    fun sendPickUpAnswer(answer: Boolean) {
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

    fun handleOrientationChange(pendingHangup: Boolean, activity: Activity) {
        handleTitlebarView(pendingHangup = pendingHangup, activity = activity)
    }

    fun call() {
        webRTCView.call()
    }

    private fun isInCall() = webRTCView.isInCall

    fun hangUp() {
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



