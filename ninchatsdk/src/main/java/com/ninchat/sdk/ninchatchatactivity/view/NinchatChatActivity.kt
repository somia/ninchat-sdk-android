package com.ninchat.sdk.ninchatchatactivity.view

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.facebook.react.modules.core.PermissionListener
import com.ninchat.sdk.R
import com.ninchat.sdk.activities.NinchatBaseActivity
import com.ninchat.sdk.managers.IOrientationManager
import com.ninchat.sdk.ninchatchatactivity.presenter.NinchatChatPresenter
import com.ninchat.sdk.ninchatchatactivity.presenter.NinchatChatPresenter.Companion.PICK_PHOTO_VIDEO_REQUEST_CODE
import com.ninchat.sdk.ninchatreview.model.NinchatReviewModel
import com.ninchat.sdk.ninchatreview.presenter.NinchatReviewPresenter
import com.ninchat.sdk.ninchattitlebar.view.NinchatTitlebarView
import com.ninchat.sdk.utils.keyboard.hideKeyBoardForce
import com.ninchat.sdk.utils.misc.Broadcast
import com.ninchat.sdk.utils.misc.NinchatLinearLayoutManager
import kotlinx.android.synthetic.main.activity_ninchat_chat.*
import kotlinx.android.synthetic.main.dialog_close_chat.*
import org.jitsi.meet.sdk.JitsiMeetActivityInterface

class NinchatChatActivity : NinchatBaseActivity(), IOrientationManager, JitsiMeetActivityInterface {
    private val presenter by lazy {
        NinchatChatPresenter()
    }
    private val channelCloseReceiver = presenter.channelCloseReceiver(
        onChatClosed = {
            presenter.layoutModel.chatClosed = true
            send_message_container.isEnabled = false
        },
        onHideKeyboard = {
            hideKeyBoardForce()
        }
    )

    private val transferReceiver = presenter.transferReceiver(
        onCloseActivity = {
            quit(it)
        }
    )

    private val webRTCMessageReceiver = presenter.webrtcMessageReceiver(
        onP2PCall = {},
        onGroupCall = {}
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

    private fun chatClosed() {
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
        LocalBroadcastManager.getInstance(this).let {
            it.registerReceiver(channelCloseReceiver, IntentFilter(Broadcast.CHANNEL_CLOSED))
            it.registerReceiver(transferReceiver, IntentFilter(Broadcast.AUDIENCE_ENQUEUED))
            it.registerReceiver(webRTCMessageReceiver, IntentFilter(Broadcast.WEBRTC_MESSAGE))
        }
        presenter.loadMessageHistory()
        updateVisibility()
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).let {
            it.unregisterReceiver(channelCloseReceiver)
            it.unregisterReceiver(transferReceiver)
            it.unregisterReceiver(webRTCMessageReceiver)
        }
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
                    chatClosed()
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
}

fun Activity.quit(intent: Intent?) {
    val data = intent ?: Intent()
    setResult(Activity.RESULT_OK, data)
    finish()
}