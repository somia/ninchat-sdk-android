package com.ninchat.sdk.ninchatchatactivity.presenter

import android.content.pm.ActivityInfo
import android.hardware.SensorManager
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.managers.IOrientationManager
import com.ninchat.sdk.managers.OrientationManager
import com.ninchat.sdk.networkdispatchers.NinchatDeleteUser
import com.ninchat.sdk.networkdispatchers.NinchatDiscoverJitsi
import com.ninchat.sdk.networkdispatchers.NinchatPartChannel
import com.ninchat.sdk.networkdispatchers.NinchatSendMessage
import com.ninchat.sdk.ninchatchatactivity.model.NinchatChatModel
import com.ninchat.sdk.ninchatchatactivity.view.NinchatChatActivity
import com.ninchat.sdk.utils.messagetype.NinchatMessageTypes
import com.ninchat.sdk.utils.misc.Misc
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
import com.ninchat.sdk.utils.writingindicator.WritingIndicator
import kotlinx.android.synthetic.main.activity_ninchat_chat.view.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.json.JSONObject

class NinchatChatPresenter(
    val model: NinchatChatModel,
) {
    val writingIndicator = WritingIndicator()
    lateinit var orientationManager: OrientationManager

    fun initialize(ninchatChatActivity: NinchatChatActivity, callback: IOrientationManager) {
        orientationManager = OrientationManager(
            callback,
            ninchatChatActivity,
            SensorManager.SENSOR_DELAY_NORMAL
        ).apply {
            enable()
        }

    }

    val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            writingIndicator.updateLastWritingTime(s.length)
        }
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        NinchatSessionManager.getInstance()?.sessionError(Exception(exception))
    }

    fun handleOrientationChange(currentOrientation: Int, mActivity: NinchatChatActivity) {
        if (model.toggleFullScreen) {
            return
        }
        try {
            if (Settings.System.getInt(
                    mActivity.applicationContext.contentResolver,
                    Settings.System.ACCELEROMETER_ROTATION,
                    0
                ) != 1
            ) return
        } catch (e: java.lang.Exception) {
            // pass
        }

        if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            mActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    fun sendMessage(view: View) {
        if(model.chatClosed) return
        view.message?.text?.toString()?.let { message ->
            if (message.isEmpty()) {
                return
            }
            NinchatSessionManager.getInstance()?.let { currentSessionManager ->
                NinchatScopeHandler.getIOScope().launch(exceptionHandler) {
                    NinchatSendMessage.execute(
                        currentSession = currentSessionManager.session,
                        channelId = currentSessionManager.ninchatState?.channelId,
                        message = JSONObject().apply {
                            put("text", message)
                        }.toString(),
                        messageType = NinchatMessageTypes.TEXT
                    )
                }
            }
        }
        writingIndicator.notifyBackend(false)
        view.message?.text?.clear()
    }

    fun onActivityClose() {
        NinchatSessionManager.getInstance()?.let { ninchatSessionManager ->
            if(Misc.shouldPartChannel(ninchatSessionManager.ninchatState)) {
                NinchatScopeHandler.getIOScope().launch(exceptionHandler) {
                    NinchatPartChannel.execute(
                        currentSession = ninchatSessionManager.session,
                        channelId = ninchatSessionManager.ninchatState?.channelId,
                    )
                }
            }
            // delete user if the current user is a guest user
            if (NinchatSessionManager.getInstance().isGuestMember) {
                NinchatScopeHandler.getIOScope().launch(exceptionHandler) {
                    NinchatDeleteUser.execute(
                        currentSession = NinchatSessionManager.getInstance().session,
                    )
                }
            }
        }
    }

    companion object {
        @kotlin.jvm.JvmField
        val REQUEST_CODE = NinchatChatPresenter::class.hashCode() and 0xffff

        @kotlin.jvm.JvmField
        val CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE =
            "WebRTCVideoAudio".hashCode() and 0xffff

        @kotlin.jvm.JvmField
        val PICK_PHOTO_VIDEO_REQUEST_CODE = "PickPhotoVideo".hashCode() and 0xffff
    }
}