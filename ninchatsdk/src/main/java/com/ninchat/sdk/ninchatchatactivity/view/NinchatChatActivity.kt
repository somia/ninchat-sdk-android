package com.ninchat.sdk.ninchatchatactivity.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.app.AlertDialog;
import android.content.pm.PackageManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.facebook.react.modules.core.PermissionListener
import com.ninchat.sdk.R
import com.ninchat.sdk.activities.NinchatBaseActivity
import com.ninchat.sdk.events.OnCloseChat
import com.ninchat.sdk.helper.glidewrapper.GlideWrapper
import com.ninchat.sdk.managers.IOrientationManager
import com.ninchat.sdk.ninchatchatactivity.presenter.NinchatChatPresenter
import com.ninchat.sdk.ninchatchatactivity.presenter.NinchatChatPresenter.Companion.CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE
import com.ninchat.sdk.ninchatchatactivity.presenter.NinchatChatPresenter.Companion.PICK_PHOTO_VIDEO_REQUEST_CODE
import com.ninchat.sdk.ninchatintegrations.p2p.NinchatP2PIntegration
import com.ninchat.sdk.ninchatreview.model.NinchatReviewModel
import com.ninchat.sdk.ninchatreview.presenter.NinchatReviewPresenter
import com.ninchat.sdk.ninchattitlebar.view.NinchatTitlebarView
import com.ninchat.sdk.utils.keyboard.hideKeyBoardForce
import com.ninchat.sdk.utils.misc.Broadcast
import com.ninchat.sdk.utils.misc.NinchatLinearLayoutManager
import com.ninchat.sdk.utils.misc.Parameter
import kotlinx.android.synthetic.main.activity_ninchat_chat.*
import kotlinx.android.synthetic.main.dialog_close_chat.*
import kotlinx.android.synthetic.main.dialog_video_call_consent.*
import kotlinx.android.synthetic.main.ninchat_video_view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jitsi.meet.sdk.JitsiMeetActivityInterface

class NinchatChatActivity : NinchatBaseActivity(), IOrientationManager, JitsiMeetActivityInterface {
    private val presenter by lazy {
        NinchatChatPresenter()
    }
    private val p2pView by lazy {
        NinchatP2PIntegration(videoContainer, onToggleFullScreen = {

        })
    }
    private val broadcastReceiver = presenter.activityBroadcastReceiver(
        onChatClosed = {
            presenter.layoutModel.chatClosed = true
            send_message_container.isEnabled = false
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
        onGroupCall = {
            attachJitsiVideoView()
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
        presenter.layoutModel.chatClosed = true
        send_message_container.isEnabled = false
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
    }

    override fun onStart() {
        super.onStart()
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
        EventBus.getDefault().unregister(this)
        LocalBroadcastManager.getInstance(this).apply {
            unregisterReceiver(broadcastReceiver)
        }
    }

    override fun onDestroy() {
        p2pView.onDestroy()
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
        videoContainer?.apply {
            ninchat_video_layout.visibility =
                if (presenter.layoutModel.isGroupCall) View.GONE else View.VISIBLE
            ninchat_jitsi_layout.visibility =
                if (presenter.layoutModel.isGroupCall) View.VISIBLE else View.GONE
        }

    }

    override fun requestPermissions(p0: Array<out String>?, p1: Int, p2: PermissionListener?) {
        TODO("Not yet implemented")
    }

    override fun onOrientationChange(orientation: Int) {
        TODO("Not yet implemented")
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

    private fun attachJitsiVideoView() {

    }

    private fun hasVideoCallPermissions(): Boolean {
        return checkCallingOrSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }


}

fun Activity.quit(intent: Intent?) {
    val data = intent ?: Intent()
    setResult(Activity.RESULT_OK, data)
    finish()
}