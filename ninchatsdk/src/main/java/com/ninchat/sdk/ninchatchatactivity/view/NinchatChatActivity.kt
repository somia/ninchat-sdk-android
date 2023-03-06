package com.ninchat.sdk.ninchatchatactivity.view

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.hardware.SensorManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.react.modules.core.PermissionListener
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.activities.NinchatBaseActivity
import com.ninchat.sdk.adapters.NinchatMessageAdapter
import com.ninchat.sdk.events.OnNewMessage
import com.ninchat.sdk.managers.IOrientationManager
import com.ninchat.sdk.managers.OrientationManager
import com.ninchat.sdk.ninchatchatactivity.model.NinchatChatModel
import com.ninchat.sdk.ninchatchatactivity.presenter.NinchatChatPresenter
import com.ninchat.sdk.ninchatreview.model.NinchatReviewModel
import com.ninchat.sdk.ninchatreview.presenter.NinchatReviewPresenter
import com.ninchat.sdk.ninchattitlebar.model.shouldShowTitlebar
import com.ninchat.sdk.ninchattitlebar.view.NinchatTitlebarView.Companion.showTitlebarForBacklog
import com.ninchat.sdk.ninchatvideointegrations.jitsi.NinchatGroupCallIntegration
import com.ninchat.sdk.ninchatvideointegrations.p2p.NinchatP2PIntegration
import com.ninchat.sdk.utils.keyboard.hideKeyBoardForce
import com.ninchat.sdk.utils.misc.Broadcast
import com.ninchat.sdk.utils.misc.Misc.Companion.getNinchatChatBackground
import com.ninchat.sdk.utils.misc.NinchatAdapterCallback
import com.ninchat.sdk.utils.misc.NinchatLinearLayoutManager
import com.ninchat.sdk.utils.misc.Parameter
import kotlinx.android.synthetic.main.activity_ninchat_chat.*
import kotlinx.android.synthetic.main.activity_ninchat_chat.view.*
import kotlinx.android.synthetic.main.ninchat_titlebar.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jitsi.meet.sdk.BroadcastEvent
import org.jitsi.meet.sdk.JitsiMeetActivityDelegate
import org.jitsi.meet.sdk.JitsiMeetActivityInterface
import org.jitsi.meet.sdk.JitsiMeetView

class NinchatChatActivity : NinchatBaseActivity(), IOrientationManager, JitsiMeetActivityInterface {
    private var p2pIntegration: NinchatP2PIntegration? = null
    private var groupIntegration: NinchatGroupCallIntegration? = null
    private val model = NinchatChatModel().apply {
        parse()
    }
    private val presenter = NinchatChatPresenter(model)
    private val mBroadcastManager = NinchatChatBroadcastManager(
        ninchatChatActivity = this@NinchatChatActivity,
        onChannelClosed = {
            // common updates
            presenter.onChannelClosed(mActivity = this@NinchatChatActivity)
            // p2p updates
            p2pIntegration?.onChannelClosed()
            // group updates
            groupIntegration?.onChannelClosed()
        },
        onTransfer = {
            quit(it)
        },
        onP2PVideoCallInvitation = {
            p2pIntegration?.maybeHandleP2PVideoCallInvitation(
                intent = it,
                activity = this@NinchatChatActivity
            )
            p2pIntegration?.mayBeHandleWebRTCMessages(
                intent = it,
                activity = this@NinchatChatActivity,
            )
        },
        onJitsiDiscovered = {
            val jitsiRoom = it.extras?.getString(Broadcast.WEBRTC_MESSAGE_JITSI_ROOM) ?: ""
            val jitsiToken = it.extras?.getString(Broadcast.WEBRTC_MESSAGE_JITSI_TOKEN) ?: ""
            val jitsiServerAddress =
                it.extras?.getString(Broadcast.WEBRTC_MESSAGE_JITSI_SERVER) ?: ""


            groupIntegration?.startJitsi(
                jitsiRoom = jitsiRoom,
                jitsiToken = jitsiToken,
                jitsiServerAddress = jitsiServerAddress,
            )

        },
        onJitsiConferenceEvents = { intent ->
            if (intent == null) return@NinchatChatBroadcastManager
            val event = BroadcastEvent(intent)
            Log.e("JITSI EVENT", "${event.type}")
            when (event.type) {
                BroadcastEvent.Type.CONFERENCE_TERMINATED, BroadcastEvent.Type.READY_TO_CLOSE -> {
                    groupIntegration?.onHangup()
                }
            }
        }
    )
    private val softKeyboardViewHandler = SoftKeyboardViewHandler(
        onShow = {
            p2pIntegration?.onSoftKeyboardVisibilityChanged(isVisible = true)
            groupIntegration?.onSoftKeyboardVisibilityChanged(
                isVisible = true,
            )
            presenter.onSoftKeyboardVisibilityChanged(isVisible = true)
        },
        onHidden = {
            p2pIntegration?.onSoftKeyboardVisibilityChanged(isVisible = false)
            groupIntegration?.onSoftKeyboardVisibilityChanged(
                isVisible = false,
            )
            presenter.onSoftKeyboardVisibilityChanged(isVisible = false)
        },
    )

    lateinit var orientationManager: OrientationManager

    override fun onOrientationChange(orientation: Int) {
        // if user manually toggle to full screen then don't change orientation
        if (model.toggleFullScreen) {
            return
        }
        presenter.handleOrientationChange(orientation, this@NinchatChatActivity)
        p2pIntegration?.handleOrientationChange(
            currentOrientation = orientation,
            pendingHangup = false,
        )
        groupIntegration?.handleOrientationChange(currentOrientation = orientation)
    }

    private fun quit(data: Intent?) {
        val newData = data ?: Intent()
        setResult(RESULT_OK, newData)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == NinchatReviewModel.REQUEST_CODE) {
            // coming from ninchat review
            presenter.onActivityClose()
            quit(data)
        } else if (requestCode == NinchatChatPresenter.PICK_PHOTO_VIDEO_REQUEST_CODE && resultCode == RESULT_OK) {
            presenter.onAlbumSelected(data!!, applicationContext)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == NinchatChatPresenter.CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE) {
            if (p2pIntegration?.hasVideoCallPermissions() == true) {
                p2pIntegration?.sendPickUpAnswer(true)
            } else {
                p2pIntegration?.sendPickUpAnswer(false)
                showError(
                    R.id.ninchat_chat_error,
                    R.string.ninchat_chat_error_no_video_call_permissions
                )
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (hasFileAccessPermissions()) {
                openImagePicker(null)
            } else {
                showError(R.id.ninchat_chat_error, R.string.ninchat_chat_error_no_file_permissions)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun requestPermissions(
        permissions: Array<out String>?,
        requestCode: Int,
        listener: PermissionListener?
    ) {
        JitsiMeetActivityDelegate.requestPermissions(this, permissions, requestCode, listener)
    }

    fun onCloseChat(view: View?) {
        NinchatChatCloseDialogue.show(
            context = this@NinchatChatActivity,
            onAccept = {
                chatClosed()
            },
            isChatClosed = model.chatClosed
        )
    }

    // only happen for group video call
    fun onToggleChat(view: View?) {
        groupIntegration?.onToggleChat(mActivity = this@NinchatChatActivity)
    }

    fun onVideoHangUp(view: View?) {
        model.toggleFullScreen = false
        // handle p2p hangup
        p2pIntegration?.handleTitlebarView(true, this)
        p2pIntegration?.hangUp()
        // handle group hangup
        groupIntegration?.hangUp()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
    }

    // only happen for p2p chat
    fun onToggleFullScreen(view: View?) {
        model.toggleFullScreen = !model.toggleFullScreen
        val nextOrientation =
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        requestedOrientation = nextOrientation
        p2pIntegration?.handleOrientationChange(
            currentOrientation = nextOrientation,
            pendingHangup = false,
        )
    }

    fun chatClosed() {
        onVideoHangUp(null)
        val showRatings =
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.showRating() ?: false
        if (showRatings) {
            startActivityForResult(
                NinchatReviewPresenter.getLaunchIntent(this@NinchatChatActivity),
                NinchatReviewModel.REQUEST_CODE
            )
        } else {
            quit(null)
        }
    }

    fun onToggleAudio(view: View?) {
        p2pIntegration?.toggleAudio()
    }

    fun onToggleMicrophone(view: View?) {
        p2pIntegration?.toggleMicrophone()
    }

    fun onToggleVideo(view: View?) {
        p2pIntegration?.toggleVideo()
    }

    fun onVideoCall(view: View?) {
        if (model.chatClosed) {
            return
        }
        p2pIntegration?.call()
    }

    fun onAttachmentClick(view: View?) {
        if (model.chatClosed) {
            return
        }
        if (hasFileAccessPermissions()) {
            openImagePicker(view)
        } else {
            requestFileAccessPermissions()
        }
    }

    private fun openImagePicker(view: View?) {
        startActivityForResult(
            Intent(Intent.ACTION_PICK).setType("image/*"),
            NinchatChatPresenter.PICK_PHOTO_VIDEO_REQUEST_CODE
        )
    }

    private fun initializeClosedChat(messages: RecyclerView) {
        NinchatSessionManager.getInstance()?.let { sessionManager ->
            // Wait for RecyclerView to be initialized
            messages.viewTreeObserver.addOnGlobalLayoutListener {

                // Close chat if it hasn't been closed yet
                if (!model.chatClosed && model.historyLoaded) {
                    sessionManager.getOnInitializeMessageAdapter(object : NinchatAdapterCallback {
                        override fun onMessageAdapter(adapter: NinchatMessageAdapter) {
                            adapter.close(
                                this@NinchatChatActivity
                            )
                        }
                    })
                    model.chatClosed = true
                    groupIntegration?.onChannelClosed()
                    hideKeyBoardForce()
                }

                // Initialize closed chat with recent messages only
                if (!model.historyLoaded) {
                    sessionManager.loadChannelHistory(null)
                    model.historyLoaded = true
                }
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        // If the app is killed in the background sessionManager is not initialized the SDK must
        // be exited and the NinchatSession needs to be initialzed again
        val sessionManager = NinchatSessionManager.getInstance() ?: run {
            setResult(RESULT_CANCELED, null)
            finish()
            overridePendingTransition(0, 0)
            return
        }
        if (resources.getBoolean(R.bool.ninchat_chat_background_not_tiled)) {
            ninchat_chat_root.setBackgroundResource(sessionManager.ninchatChatBackground)
        } else {
            getNinchatChatBackground(
                applicationContext,
                sessionManager.ninchatChatBackground
            )?.let {
                ninchat_chat_root.background = it
            }
        }

        // start with orientation toggled false
        model.toggleFullScreen = false
        if (model.isGroupCall) {
            groupIntegration = NinchatGroupCallIntegration(
                jitsiMeetView = JitsiMeetView(this),
                mActivity = this@NinchatChatActivity,
                chatClosed = model.chatClosed
            )
        } else {
            p2pIntegration =
                NinchatP2PIntegration(
                    videoContainer = conference_or_p2p_view_container.findViewById(R.id.ninchat_p2p_video_view),
                    mActivity = this@NinchatChatActivity,
                )
        }
        mBroadcastManager.register(LocalBroadcastManager.getInstance(applicationContext))
        message_list.layoutManager = NinchatLinearLayoutManager(
            applicationContext
        )
        sessionManager.getOnInitializeMessageAdapter(object : NinchatAdapterCallback {
            override fun onMessageAdapter(adapter: NinchatMessageAdapter) {
                message_list.adapter = adapter
            }
        })
        message.apply {
            hint = sessionManager.ninchatState.siteConfig.getEnterMessageText()
            addTextChangedListener(presenter.textWatcher)
        }
        presenter.writingIndicator.initiate()
        ninchat_chat_close.apply {
            text = sessionManager.ninchatState.siteConfig.getChatCloseText()
            // if it is a group chat, then close button should be hidden
            if (!model.isGroupCall && !shouldShowTitlebar()) {
                visibility = View.VISIBLE
            }
        }
        send_button.apply {
            val sendButtonText = sessionManager.ninchatState.siteConfig.getSendButtonText()
            if (sendButtonText != null) {
                text = sendButtonText
            } else {
                visibility = View.GONE
                send_button_icon.visibility = View.VISIBLE
            }
        }
        attachment.visibility =
            if (sessionManager.ninchatSessionHolder.supportFiles()) View.VISIBLE else View.GONE
        video_call.visibility =
            if (sessionManager.ninchatSessionHolder.supportVideos() && resources.getBoolean(R.bool.ninchat_allow_user_initiated_video_calls)) View.VISIBLE else View.GONE
        if (intent.extras?.getBoolean(Parameter.CHAT_IS_CLOSED) == true) {
            initializeClosedChat(message_list)
        } else {
            initializeChat(message_list)
        }
        showTitlebarForBacklog(view = ninchat_chat_root.findViewById(R.id.ninchat_titlebar),
            callback = {
                onCloseChat(null)
            },
            onToggleChat = {
                groupIntegration?.onToggleChat(mActivity = this@NinchatChatActivity)
            }
        )
        // Set up a soft keyboard visibility listener so video call container height can be adjusted
        softKeyboardViewHandler.register(findViewById(android.R.id.content))
        // setup orientation manager
        orientationManager = OrientationManager(
            callback = this,
            activity = this,
            rate = SensorManager.SENSOR_DELAY_NORMAL
        ).apply {
            enable()
        }
    }

    private fun initializeChat(messages: RecyclerView) {
        NinchatSessionManager.getInstance()?.let { sessionManager ->
            // Wait for RecyclerView to be initialized
            messages.viewTreeObserver.addOnGlobalLayoutListener {
                if (!model.chatClosed) {
                    sessionManager.getOnInitializeMessageAdapter(object : NinchatAdapterCallback {
                        override fun onMessageAdapter(adapter: NinchatMessageAdapter) {
                            adapter.removeChatCloseMessage()
                        }
                    })
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        // Refresh the message list, just in case
        val sessionManager = NinchatSessionManager.getInstance() ?: return
        sessionManager.getOnInitializeMessageAdapter(object : NinchatAdapterCallback {
            override fun onMessageAdapter(adapter: NinchatMessageAdapter) {
                adapter.notifyDataSetChanged()
            }
        })
        // Don't load first messages if chat is closed, we want to load the latest messages only
        if (intent.extras?.getBoolean(Parameter.CHAT_IS_CLOSED) == false) {
            sessionManager.getOnInitializeMessageAdapter(object : NinchatAdapterCallback {
                override fun onMessageAdapter(adapter: NinchatMessageAdapter) {
                    sessionManager.loadChannelHistory(
                        adapter.getLastMessageId(false)
                    )
                }
            })
        }
    }

    override fun onDestroy() {
        onVideoHangUp(null)
        val localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        mBroadcastManager.unregister(localBroadcastManager)
        softKeyboardViewHandler.unregister()
        presenter.writingIndicator.dispose()
        orientationManager.disable()
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    fun onEditTextClick(view: View?) {
        if (message.requestFocus()) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(message, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun onSendClick(view: View?) {
        presenter.sendMessage(send_message_container)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    @JvmName("OnNewMessage")
    fun onNewMessage(onNewMessage: OnNewMessage) {
        // show indicator that a new chat message has appeared
        groupIntegration?.onNewMessage(view = ninchat_titlebar)
    }

    override val layoutRes: Int
        get() = R.layout.activity_ninchat_chat


}