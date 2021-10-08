package com.ninchat.sdk.ninchatchatactivity.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.app.AlertDialog;
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.SensorManager
import android.view.OrientationEventListener
import android.view.Surface
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.facebook.react.modules.core.PermissionListener
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.activities.NinchatBaseActivity
import com.ninchat.sdk.events.OnCloseChat
import com.ninchat.sdk.helper.glidewrapper.GlideWrapper
import com.ninchat.sdk.managers.IOrientationManager
import com.ninchat.sdk.managers.OrientationManager
import com.ninchat.sdk.ninchatchatactivity.presenter.NinchatChatPresenter
import com.ninchat.sdk.ninchatchatactivity.presenter.NinchatChatPresenter.Companion.CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE
import com.ninchat.sdk.ninchatchatactivity.presenter.NinchatChatPresenter.Companion.JITSI_CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE
import com.ninchat.sdk.ninchatchatactivity.presenter.NinchatChatPresenter.Companion.PICK_PHOTO_VIDEO_REQUEST_CODE
import com.ninchat.sdk.ninchatintegrations.jitsi.NinchatJitsiIntegration
import com.ninchat.sdk.ninchatintegrations.p2p.NinchatP2PIntegration
import com.ninchat.sdk.ninchatreview.model.NinchatReviewModel
import com.ninchat.sdk.ninchatreview.presenter.NinchatReviewPresenter
import com.ninchat.sdk.ninchattitlebar.model.shouldShowTitlebar
import com.ninchat.sdk.ninchattitlebar.view.NinchatTitlebarView
import com.ninchat.sdk.utils.keyboard.hideKeyBoardForce
import com.ninchat.sdk.utils.misc.Broadcast
import com.ninchat.sdk.utils.misc.NinchatLinearLayoutManager
import com.ninchat.sdk.utils.misc.Parameter
import kotlinx.android.synthetic.main.activity_ninchat_chat.*
import kotlinx.android.synthetic.main.dialog_close_chat.*
import kotlinx.android.synthetic.main.dialog_video_call_consent.*
import kotlinx.android.synthetic.main.ninchat_video_view.*
import kotlinx.android.synthetic.main.ninchat_video_view.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jitsi.meet.sdk.JitsiMeetActivityDelegate
import org.jitsi.meet.sdk.JitsiMeetActivityInterface
import org.jitsi.meet.sdk.JitsiMeetView
import android.provider.Settings.System.ACCELEROMETER_ROTATION
import java.lang.Exception
import android.provider.Settings;

class NinchatChatActivity : NinchatBaseActivity(), IOrientationManager, JitsiMeetActivityInterface {
    private val presenter by lazy {
        NinchatChatPresenter()
    }
    private val p2pView by lazy {
        NinchatP2PIntegration(videoContainer, onToggleFullScreen = {
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        })
    }
    private val groupView by lazy {
        NinchatJitsiIntegration(videoContainer, JitsiMeetView(this))
    }
    private val orientationManager by lazy {
        OrientationManager(this, this, SensorManager.SENSOR_DELAY_UI)

    }

    private val broadcastReceiver = presenter.activityBroadcastReceiver(
        onChatClosed = {
            if (!presenter.layoutModel.chatClosed) {
                if (presenter.layoutModel.isGroupCall) groupView.hangUp()
                else p2pView.hangUp()
            }
            presenter.layoutModel.chatClosed = true
            disable()
        },
        onHideKeyboard = {
            hideKeyBoardForce()
        },
        onCloseActivity = {
            quit(it)
        },
        onP2PCall = {
            attachP2pVideoView()
        },
        onGroupCall = { jitsiRoom, jitsiToken, jitsiServerPrefix ->
            attachJitsiVideoView(jitsiRoom, jitsiToken, jitsiServerPrefix)
        },
        onWebRTCEvents = { mediaType, payload ->
            p2pView.handleRTCMessage(
                mediaType,
                payload,
                onHandUp = { p2pView.hangUp() }
            )
        }
    )

    override val layoutRes: Int
        get() = R.layout.activity_ninchat_chat

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            presenter.isReviewActivityResult(requestCode) -> presenter.onReviewActivityResult {
                quit(data)
            }
            presenter.isPhotoPickedResult(requestCode, resultCode) -> presenter.onPhotoPickedResult(
                intent = data,
                contentResolver = contentResolver
            )
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @JvmName("OnCloseChat")
    fun chatClosed(onCloseChat: OnCloseChat) {
        if (presenter.layoutModel.isGroupCall)
            groupView.onDestroy()
        else
            p2pView.onDestroy()
        presenter.layoutModel.chatClosed = true
        disable()
        if (presenter.layoutModel.showRatingView) {
            startActivityForResult(
                NinchatReviewPresenter.getLaunchIntent(this@NinchatChatActivity),
                NinchatReviewModel.REQUEST_CODE
            );
        } else {
            quit(null);
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        message_list.let {
            it.layoutManager = NinchatLinearLayoutManager(context = applicationContext)
            it.adapter = presenter.messageAdapter()
        }
        ninchat_message_send_button.setOnClickListener { onSendButtonClicked() }
        ninchat_message_send_button_icon.setOnClickListener { onSendButtonClicked() }
        val chatClosed = intent.extras?.getBoolean(Parameter.CHAT_IS_CLOSED, false) ?: false
        if (!chatClosed && presenter.layoutModel.isGroupCall) {
            if (hasVideoCallPermissions()) {
                handleVisibility()
                presenter.loadJitsi()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                    JITSI_CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE
                );
            }
        }
    }

    override fun onStart() {
        super.onStart()
        orientationManager.enable()
        EventBus.getDefault().register(this)
        LocalBroadcastManager.getInstance(this).let {
            it.registerReceiver(broadcastReceiver, IntentFilter().apply {
                addAction(Broadcast.CHANNEL_CLOSED)
                addAction(Broadcast.AUDIENCE_ENQUEUED)
                addAction(Broadcast.WEBRTC_MESSAGE)
            })
        }
        presenter.layoutModel.chatClosed =
            intent.extras?.getBoolean(Parameter.CHAT_IS_CLOSED, false) ?: false
        presenter.loadMessageHistory()
        updateVisibility()
    }

    override fun onStop() {
        super.onStop()
        orientationManager.disable()
        EventBus.getDefault().unregister(this)
        LocalBroadcastManager.getInstance(this).apply {
            unregisterReceiver(broadcastReceiver)
        }
    }

    override fun onDestroy() {
        if (presenter.layoutModel.isGroupCall)
            groupView.onDestroy()
        else
            p2pView.onDestroy()
        orientationManager.disable()
        super.onDestroy()
    }

    private fun updateVisibility() {
        ninchat_message_send_button_icon?.apply {
            visibility = if (presenter.layoutModel.showSendButtonIcon) View.VISIBLE else View.GONE
        }
        ninchat_message_send_button?.apply {
            visibility = if (presenter.layoutModel.showSendButtonText) View.VISIBLE else View.GONE
            text = presenter.layoutModel.sendButtonText
        }
        attachment?.apply {
            visibility = if (presenter.layoutModel.showAttachment) View.VISIBLE else View.GONE
            setOnClickListener {
                mayBeOpenFiles(showError = false)
            }
        }
        video_call?.apply {
            visibility = if (presenter.layoutModel.showVideoCalls) View.VISIBLE else View.GONE
            setOnClickListener {
                p2pView.call()
            }
        }
        ninchat_titlebar?.apply {
            visibility = if (presenter.layoutModel.showTitlebar) View.VISIBLE else View.GONE
            NinchatTitlebarView.showTitlebarForBacklog(
                view = this,
                callback = {
                    showChatCloseDialog()
                }
            )
        }
        ninchat_chat_close?.apply {
            visibility = if (presenter.layoutModel.showTitlebar) View.GONE else View.VISIBLE
            text = presenter.layoutModel.chatCloseText
            setOnClickListener {
                showChatCloseDialog()
            }
        }
        if (presenter.layoutModel.chatClosed) {
            disable()
        }
    }

    override fun requestPermissions(
        permissions: Array<out String>?,
        requestCode: Int,
        listener: PermissionListener?
    ) {
        JitsiMeetActivityDelegate.requestPermissions(this, permissions, requestCode, listener)
    }

    private fun disable() {
        listOf(
            attachment,
            video_call,
            ninchat_message_send_button_icon,
            ninchat_message_send_button,
            ninchat_message_input
        ).onEach {
            it.isEnabled = false
        }
    }

    override fun onOrientationChange(orientation: Int) {
        handleOrientationChange(orientation)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> {
                mayBeOpenFiles(showError = hasFileAccessPermissions())
            }
            CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE -> {
                if (hasVideoCallPermissions()) {
                    presenter.sendPickUpAnswer(true)
                } else {
                    presenter.sendPickUpAnswer(false)
                    showError(
                        R.id.ninchat_chat_error,
                        R.string.ninchat_chat_error_no_video_call_permissions
                    )
                }
            }
            JITSI_CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE -> {
                if (hasVideoCallPermissions()) {
                    handleVisibility()
                    presenter.loadJitsi()
                } else {
                    showError(
                        R.id.ninchat_chat_error,
                        R.string.ninchat_chat_error_no_video_call_permissions
                    )
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private val onSendButtonClicked = {
        ninchat_message_input.let {
            val value = it.text?.toString() ?: ""
            if (value.isNotEmpty()) {
                presenter.sendMessage(value)
            }
            it.text = null
        }
    }

    private fun showChatCloseDialog() {
        val dialog = AlertDialog.Builder(this, R.style.NinchatTheme_Dialog)
            .setView(R.layout.dialog_close_chat)
            .setCancelable(true)
            .create()
        dialog.run {
            show()
            ninchat_close_chat_dialog_title.text = presenter.layoutModel.chatCloseText
            ninchat_close_chat_dialog_description.text =
                presenter.layoutModel.chatCloseConfirmationText

            ninchat_close_chat_dialog_confirm.also { btn ->
                btn.text = presenter.layoutModel.chatCloseText
                btn.setOnClickListener {
                    chatClosed(OnCloseChat())
                    dialog.dismiss()
                }
            }
            ninchat_close_chat_dialog_decline.also { btn ->
                btn.text = presenter.layoutModel.chatCloseDeclineText
                btn.setOnClickListener { dialog.dismiss() }
            }
        }
    }

    private fun mayBeOpenFiles(showError: Boolean = false) {
        when {
            hasFileAccessPermissions() -> {
                startActivityForResult(
                    Intent(Intent.ACTION_PICK).setType("image/*"),
                    PICK_PHOTO_VIDEO_REQUEST_CODE
                );
            }
            showError -> {
                showError(
                    R.id.ninchat_chat_error,
                    R.string.ninchat_chat_error_no_file_permissions
                );
            }
            else -> {
                requestFileAccessPermissions()
            }
        }
    }

    private fun attachP2pVideoView() {
        val userId = intent.getStringExtra(Broadcast.WEBRTC_MESSAGE_SENDER) ?: ""
        val currentUser = presenter.getUser(userId = userId)
        val dialog = AlertDialog.Builder(this, R.style.NinchatTheme_Dialog)
            .setView(R.layout.dialog_video_call_consent)
            .setCancelable(false)
            .create()
        dialog.run {
            show()
            ninchat_video_call_consent_dialog_title.text = presenter.layoutModel.videoCallTitleText
            ninchat_video_call_consent_dialog_description.text =
                presenter.layoutModel.videoCallDescriptionText
            ninchat_video_call_consent_dialog_accept.also { btn ->
                btn.text = presenter.layoutModel.videoCallAcceptText
                btn.setOnClickListener {
                    dialog.dismiss()
                    if (hasVideoCallPermissions()) {
                        presenter.sendPickUpAnswer(true)
                    } else {
                        requestPermissions(
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                            CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE
                        );
                    }
                }
            }
            ninchat_video_call_consent_dialog_decline.also { btn ->
                btn.text = presenter.layoutModel.videoCallDeclineText
                btn.setOnClickListener {
                    presenter.sendPickUpAnswer(false)
                    dismiss()
                }
            }
            ninchat_video_call_consent_dialog_user_name.text = currentUser?.name ?: ""
            presenter.getUserAvatar(currentUser)?.let {
                if (it.isNotEmpty()) {
                    GlideWrapper.loadImageAsCircle(
                        ninchat_video_call_consent_dialog_user_avatar.context,
                        it,
                        ninchat_video_call_consent_dialog_user_avatar
                    )
                }
            }
        }
    }

    private fun attachJitsiVideoView(
        jitsiRoom: String?,
        jitsiToken: String?,
        jitsiServerPrefix: String?
    ) {
        groupView.handleWebRTCMessage(
            jitsiRoom = jitsiRoom,
            jitsiToken = jitsiToken,
            jitsiVideoView = ninchat_jitsi_layout,
            serverAddress = NinchatSessionManager.getInstance()?.ninchatState?.serverAddress
                ?: "api.ninchat.com",
            width = ninchat_jitsi_layout.measuredWidth,
            height = ninchat_jitsi_layout.measuredHeight,
        )
    }

    fun handleVisibility() {
        videoContainer.visibility = View.VISIBLE
        videoContainer.ninchat_jitsi_layout.visibility = View.VISIBLE
    }

    private fun hasVideoCallPermissions(): Boolean {
        return checkCallingOrSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun handleOrientationChange(currentOrientation: Int) {
        if (!presenter.layoutModel.isGroupCall) return
        if (!shouldShowTitlebar()) return

        try {
            if (Settings.System.getInt(
                    applicationContext.contentResolver,
                    ACCELEROMETER_ROTATION,
                    0
                ) !== 1
            ) return
        } catch (e: Exception) {
            // pass
        }
        if (currentOrientation === android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else if (currentOrientation === android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        val rotation = windowManager.defaultDisplay.rotation
        when (rotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                ninchat_titlebar.visibility =View.VISIBLE
            }
            Surface.ROTATION_270, Surface.ROTATION_90 -> {
                ninchat_titlebar.visibility = View.GONE
            }
        }
    }
}


fun Activity.quit(intent: Intent?) {
    val data = intent ?: Intent()
    setResult(Activity.RESULT_OK, data)
    finish()
}
