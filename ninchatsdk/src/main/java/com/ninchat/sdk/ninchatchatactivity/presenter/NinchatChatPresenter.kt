package com.ninchat.sdk.ninchatchatactivity.presenter

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.SensorManager
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.activities.NinchatChatActivity
import com.ninchat.sdk.managers.IOrientationManager
import com.ninchat.sdk.managers.OrientationManager
import com.ninchat.sdk.networkdispatchers.NinchatDiscoverJitsi
import com.ninchat.sdk.ninchatchatactivity.model.NinchatChatModel
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
import com.ninchat.sdk.utils.writingindicator.WritingIndicator
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

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

    private fun hideKeyboard(mActivity: NinchatChatActivity) {
        val inputMethodManager =
            mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        try {
            inputMethodManager.hideSoftInputFromWindow(mActivity.currentFocus?.windowToken, 0)
        } catch (e: java.lang.Exception) {
            // Ignore
        }
    }
    fun loadJitsi() {
        NinchatSessionManager.getInstance()?.let { currentSessionManager ->
            NinchatScopeHandler.getIOScope().launch(exceptionHandler) {
                NinchatDiscoverJitsi.execute(
                    currentSession = currentSessionManager.session,
                    channelId = currentSessionManager.ninchatState?.channelId,
                );
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