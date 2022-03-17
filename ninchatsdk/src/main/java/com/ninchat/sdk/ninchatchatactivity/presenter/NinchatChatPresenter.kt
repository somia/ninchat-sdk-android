package com.ninchat.sdk.ninchatchatactivity.presenter

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.adapters.NinchatMessageAdapter
import com.ninchat.sdk.models.NinchatUser
import com.ninchat.sdk.networkdispatchers.*
import com.ninchat.sdk.ninchatchatactivity.model.LayoutModel
import com.ninchat.sdk.ninchatchatactivity.view.NinchatChatActivity
import com.ninchat.sdk.ninchatreview.model.NinchatReviewModel
import com.ninchat.sdk.ninchattitlebar.model.shouldShowTitlebar
import com.ninchat.sdk.states.NinchatState
import com.ninchat.sdk.utils.messagetype.NinchatMessageTypes
import com.ninchat.sdk.utils.misc.Broadcast
import com.ninchat.sdk.utils.misc.Misc
import com.ninchat.sdk.utils.misc.NinchatAdapterCallback
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.json.JSONObject


class NinchatChatPresenter() {
    val layoutModel = LayoutModel(
        chatClosed = false,
        showSendButtonIcon = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getSendButtonText()
            .isNullOrEmpty(),
        sendButtonText = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getSendButtonText()
            ?: "",
        showSendButtonText = !NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getSendButtonText()
            .isNullOrEmpty(),
        showAttachment = NinchatSessionManager.getInstance()?.ninchatSessionHolder?.supportFiles()
            ?: false,
        showVideoCalls = NinchatSessionManager.getInstance()?.ninchatSessionHolder?.supportVideos()
            ?: false,
        isGroupCall = NinchatSessionManager.getInstance()?.ninchatSessionHolder?.isGroupVideo()
            ?: false,
        showTitlebar = shouldShowTitlebar(),
        showRatingView = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.showRating()
            ?: false,
        chatCloseText = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getChatCloseText()
            ?: "",
        chatCloseConfirmationText = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getChatCloseConfirmationText()
            ?: "",
        chatCloseDeclineText = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getContinueChatText()
            ?: "",
        videoCallTitleText = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getVideoChatTitleText()
            ?: "",
        videoCallDescriptionText = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getVideoChatDescriptionText()
            ?: "",
        videoCallAcceptText = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getVideoCallAcceptText()
            ?: "",
        videoCallDeclineText = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getVideoCallDeclineText()
            ?: "",
    )

    private fun shouldPartChannel(ninchatState: NinchatState?): Boolean {
        //1. there is no post audience questionnaire
        //2. Or if user click skipped rating
        return ninchatState?.let {
            !it.hasQuestionnaire(false) || it.skippedReview
        } ?: false

    }

    fun isReviewActivityResult(requestCode: Int) = requestCode == NinchatReviewModel.REQUEST_CODE
    fun onReviewActivityResult(onCloseActivity: () -> Unit) {
        val sessionManager = NinchatSessionManager.getInstance()
        if (shouldPartChannel(sessionManager?.ninchatState)) {
            NinchatScopeHandler.getIOScope().launch(exceptionHandler) {
                NinchatPartChannel.execute(
                    currentSession = sessionManager?.session,
                    channelId = sessionManager?.ninchatState?.channelId
                )
            }
            // delete the user if current user is a guest
            if (sessionManager.isGuestMember) {
                NinchatScopeHandler.getIOScope().launch(exceptionHandler) {
                    NinchatDeleteUser.execute(
                        currentSession = sessionManager?.session,
                    )
                }
            }
        }
        onCloseActivity()
    }

    fun isPhotoPickedResult(requestCode: Int, resultCode: Int) =
        PICK_PHOTO_VIDEO_REQUEST_CODE == requestCode && resultCode == Activity.RESULT_OK

    fun onPhotoPickedResult(intent: Intent?, contentResolver: ContentResolver) {
        NinchatSessionManager.getInstance()?.let { sessionManager ->
            intent?.data?.let { uri ->
                val fileName = Misc.getFileName(uri, contentResolver)
                val inputStream = contentResolver.openInputStream(uri)
                val buffer = ByteArray(inputStream?.available() ?: 0)
                inputStream?.read(buffer)
                inputStream?.close()
                NinchatScopeHandler.getIOScope().launch(exceptionHandler) {
                    NinchatSendFile.execute(
                        currentSession = sessionManager.session,
                        channelId = sessionManager.ninchatState?.channelId,
                        fileName = fileName,
                        data = buffer
                    )
                }
            }
        }
    }

    fun activityBroadcastReceiver(
        onChatClosed: () -> Unit,
        onHideKeyboard: () -> Unit,
        onCloseActivity: (intent: Intent?) -> Unit,
        onP2PCall: () -> Unit,
        onGroupCall: (jitsiRoom: String?, jitsiToken: String?, jitsiServerPrefix: String?) -> Unit,
        onWebRTCEvents: (messageType: String, payload: String?) -> Unit,
    ): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Broadcast.CHANNEL_CLOSED -> {
                        if (intent.action == Broadcast.CHANNEL_CLOSED && !layoutModel.chatClosed) {
                            NinchatSessionManager.getInstance()
                                ?.getOnInitializeMessageAdapter(object : NinchatAdapterCallback {
                                    override fun onMessageAdapter(adapter: NinchatMessageAdapter) {
                                        adapter.addEndMessage()
                                    }
                                })
                        }
                        onHideKeyboard()
                        onChatClosed()
                    }
                    Broadcast.AUDIENCE_ENQUEUED -> {
                        NinchatSessionManager.getInstance()?.let { sessionManager ->
                            NinchatScopeHandler.getIOScope().launch(exceptionHandler) {
                                NinchatPartChannel.execute(
                                    currentSession = sessionManager.session,
                                    channelId = sessionManager.ninchatState?.channelId
                                )
                            }
                        }
                        onCloseActivity(intent)
                    }
                    Broadcast.WEBRTC_MESSAGE -> {
                        when (val messageType =
                            intent.getStringExtra(Broadcast.WEBRTC_MESSAGE_TYPE)) {
                            NinchatMessageTypes.CALL -> {
                                onP2PCall()
                            }
                            NinchatMessageTypes.WEBRTC_JITSI_SERVER_CONFIG -> {
                                onGroupCall(
                                    intent.getStringExtra(Broadcast.WEBRTC_MESSAGE_JITSI_ROOM),
                                    intent.getStringExtra(Broadcast.WEBRTC_MESSAGE_JITSI_TOKEN),
                                    intent.getStringExtra(Broadcast.WEBRTC_MESSAGE_JITSI_SERVER_PREFIX)
                                )
                            }
                            else -> {
                                onWebRTCEvents(
                                    messageType,
                                    intent.getStringExtra(Broadcast.WEBRTC_MESSAGE_CONTENT),
                                )
                            }
                        }
                    }
                }

            }
        }
    }

    fun sendMessage(text: String) {
        NinchatSessionManager.getInstance()?.let { sessionManager ->
            val data = JSONObject().apply {
                putOpt("text", text)
            }
            NinchatScopeHandler.getIOScope().launch(exceptionHandler) {
                NinchatSendMessage.execute(
                    currentSession = sessionManager.session,
                    channelId = sessionManager.ninchatState?.channelId,
                    messageType = NinchatMessageTypes.TEXT,
                    message = data.toString()
                )
            }
        }
    }

    fun messageAdapter() = NinchatSessionManager.getInstance()?.messageAdapter
    fun loadMessageHistory() {
        NinchatSessionManager.getInstance()?.let { currentSessionManager ->
            currentSessionManager.messageAdapter?.let {
                it.removeChatCloseMessage()
                if (layoutModel.chatClosed) it.addEndMessage()
            }
            currentSessionManager.loadChannelHistory(
                currentSessionManager.messageAdapter?.getLastMessageId(true)
            )
        }
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        NinchatSessionManager.getInstance()?.sessionError(Exception(exception))
    }

    fun getUser(userId: String): NinchatUser? =
        NinchatSessionManager.getInstance()?.getMember(userId)

    fun getUserAvatar(user: NinchatUser?): String? {
        if (user?.avatar?.isEmpty() == true) return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getAgentAvatar()
        return user?.avatar
    }

    fun sendPickUpAnswer(answer: Boolean) {
        NinchatSessionManager.getInstance()?.let { currentSessionManager ->
            val data = JSONObject().apply {
                put("answer", answer)
            }
            NinchatScopeHandler.getIOScope().launch(exceptionHandler) {
                NinchatSendMessage.execute(
                    currentSession = currentSessionManager.session,
                    channelId = currentSessionManager.ninchatState?.channelId,
                    messageType = NinchatMessageTypes.PICK_UP,
                    message = data.toString()
                )
                val metaMessage =
                    if (answer) currentSessionManager.ninchatState?.siteConfig?.getVideoCallAcceptedText() else currentSessionManager.ninchatState?.siteConfig?.getVideoCallRejectedText()
                currentSessionManager.messageAdapter?.addMetaMessage(
                    currentSessionManager.messageAdapter?.getLastMessageId(true)
                        .toString() + "answer", Misc.center(metaMessage)
                )
            }
        }
    }

    fun loadJitsi() {
        NinchatSessionManager.getInstance()?.let{ currentSessionManager ->
            NinchatScopeHandler.getIOScope().launch(exceptionHandler) {
                NinchatDiscoverJitsi.execute(
                    currentSession = currentSessionManager.session,
                    channelId = currentSessionManager.ninchatState?.channelId,
                );
            }
        }
    }


    companion object {
        val REQUEST_CODE = NinchatChatActivity::class.java.hashCode() and 0xffff
        val PICK_PHOTO_VIDEO_REQUEST_CODE = "PickPhotoVideo".hashCode() and 0xffff
        val CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE = "WebRTCVideoAudio".hashCode() and 0xffff
        val JITSI_CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE = "JitsiWebRTCVideoAudio".hashCode() and 0xffff
    }
}
