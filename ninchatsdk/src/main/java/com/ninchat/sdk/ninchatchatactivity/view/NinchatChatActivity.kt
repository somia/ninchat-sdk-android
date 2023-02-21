package com.ninchat.sdk.ninchatchatactivity.view

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.react.modules.core.PermissionListener
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.activities.NinchatBaseActivity
import com.ninchat.sdk.adapters.NinchatMessageAdapter
import com.ninchat.sdk.managers.IOrientationManager
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
import org.jitsi.meet.sdk.JitsiMeetActivityDelegate
import org.jitsi.meet.sdk.JitsiMeetActivityInterface
import org.jitsi.meet.sdk.JitsiMeetView
import kotlin.math.roundToInt

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
            model.chatClosed = true
            groupIntegration?.updateView(chatClosed = model.chatClosed)
            hideKeyBoardForce()
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

            runOnUiThread {
                val height = applicationContext.resources.displayMetrics.heightPixels;
                val width = applicationContext.resources.displayMetrics.widthPixels;
                val titlebarHeight = ninchat_titlebar.measuredHeight
                val heightParent = conference_or_p2p_view_container.measuredHeight
                val heightParentMeasured = conference_or_p2p_view_container.height
                Log.e(
                    "onJitsiDiscovered",
                    "$height $width $titlebarHeight $heightParent $heightParentMeasured"
                )
                groupIntegration?.startJitsi(
                    jitsiRoom = jitsiRoom,
                    jitsiToken = jitsiToken,
                    jitsiServerAddress = jitsiServerAddress,
                    fullHeight = 1961,
                    fullWidth = width,
                )
            }

        }
    )
    private val softKeyboardViewHandler = SoftKeyboardViewHandler(
        onShow = {
            // Update video height and cache current rootview height
            p2pIntegration?.setLayoutParams(
                newHeight = resources.getDimension(R.dimen.ninchat_chat_activity_video_view_height_small)
                    .roundToInt(),
                newWidth = -1
            )
            // push messages on top of soft keyboard
            NinchatSessionManager.getInstance()
                ?.getOnInitializeMessageAdapter(object : NinchatAdapterCallback {
                    override fun onMessageAdapter(adapter: NinchatMessageAdapter) {
                        adapter.scrollToBottom(true)
                    }
                })
        },
        onHidden = {
            p2pIntegration?.setLayoutParams(
                newHeight = resources.getDimension(R.dimen.ninchat_chat_activity_video_view_height)
                    .roundToInt(),
                newWidth = -1,
            )
        },
        onHeightChange = { height ->
            val h1 = applicationContext.resources.displayMetrics.heightPixels;
            val w1 = applicationContext.resources.displayMetrics.widthPixels;
            val t1 = ninchat_titlebar.measuredHeight
            val p1 = conference_or_p2p_view_container.measuredHeight
            // Update video height and cache current rootview height
            Log.e("height change", "$height $h1 $w1 $t1 $p1")
            // groupIntegration?.changeHeight(p1)
        }
    )

    override fun onOrientationChange(orientation: Int) {
        presenter.handleOrientationChange(orientation, this@NinchatChatActivity)
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
            p2pIntegration?.onAlbumSelected(data!!, applicationContext)
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

    fun onToggleChat(view: View?) {
        chat_message_list_and_editor.also {mLayout ->
            if(mLayout.visibility == View.VISIBLE) {
                mLayout.animate().translationY(mLayout.height.toFloat()).setDuration(300).withEndAction {
                    mLayout.visibility = View.GONE
                    conference_or_p2p_view_container.visibility = View.VISIBLE
                }.start()
            } else {
                conference_or_p2p_view_container.visibility = View.GONE
                mLayout.visibility = View.VISIBLE
                mLayout.animate().translationY(0f).setDuration(300).start()
            }
        }
    }

    private fun hangUp() {
        model.toggleFullScreen = false
        p2pIntegration?.handleTitlebarView(true, this)
        val sessionManager = NinchatSessionManager.getInstance()
        if (sessionManager != null) {
            p2pIntegration?.hangUp()
        }
    }

    fun onVideoHangUp(view: View?) {
        hangUp()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
    }

    fun onToggleFullScreen(view: View?) {
        model.toggleFullScreen = !model.toggleFullScreen
        requestedOrientation =
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        p2pIntegration?.handleTitlebarView(false, this@NinchatChatActivity)
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
                    groupIntegration?.updateView(chatClosed = true)
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
        presenter.initialize(this@NinchatChatActivity, this@NinchatChatActivity)
        if (model.isGroupCall) {
            groupIntegration = NinchatGroupCallIntegration(
                joinConferenceView = conference_or_p2p_view_container.findViewById(R.id.ninchat_conference_view),
                jitsiFrameLayout = conference_or_p2p_view_container.findViewById(R.id.jitsi_frame_layout),
                jitsiMeetView = JitsiMeetView(this),
                chatClosed = model.chatClosed
            )
        } else {
            p2pIntegration =
                NinchatP2PIntegration(conference_or_p2p_view_container.findViewById(R.id.ninchat_p2p_video_view))
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
                onToggleChat(null)
            }
        )
        // Set up a soft keyboard visibility listener so video call container height can be adjusted
        softKeyboardViewHandler.register(window.decorView.findViewById<View>(android.R.id.content))
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
        hangUp()
        val localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        mBroadcastManager.unregister(localBroadcastManager)
        softKeyboardViewHandler.unregister()
        presenter.writingIndicator.dispose()
        presenter.orientationManager.disable()
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

    override val layoutRes: Int
        get() = R.layout.activity_ninchat_chat

}