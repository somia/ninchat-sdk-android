package com.ninchat.sdk.ninchatvideointegrations.jitsi

import android.content.pm.ActivityInfo
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.ninchatchatactivity.view.NinchatChatActivity
import com.ninchat.sdk.ninchatvideointegrations.jitsi.model.NinchatGroupCallModel
import com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter.NinchatGroupCallPresenter
import com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter.OnClickListener
import com.ninchat.sdk.utils.keyboard.hideKeyBoardForce
import kotlinx.android.synthetic.main.activity_ninchat_chat.*
import kotlinx.android.synthetic.main.activity_ninchat_chat.view.*
import kotlinx.android.synthetic.main.ninchat_join_end_conference.*
import kotlinx.android.synthetic.main.ninchat_titlebar.view.*
import org.jitsi.meet.sdk.BroadcastIntentHelper
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import org.jitsi.meet.sdk.JitsiMeetView
import java.net.URL

class NinchatGroupCallIntegration(
    private val jitsiMeetView: JitsiMeetView,
    private val mActivity: NinchatChatActivity,
    chatClosed: Boolean = false,
) {
    private val model = NinchatGroupCallModel(
        chatClosed = chatClosed,
    ).apply {
        parse()
    }
    private val presenter = NinchatGroupCallPresenter(model = model)
    private val onClickListener = OnClickListener(intervalInMs = 2000)

    init {
        presenter.renderInitialView(mActivity = mActivity)
        attachHandler()
    }

    private fun attachHandler() {
        mActivity.conference_join_button.setOnClickListener {
            onClickListener.onClickListener {
                presenter.onClickHandler()
            }
        }
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        mActivity.jitsi_frame_layout.addView(jitsiMeetView, params)
    }

    fun onChannelClosed() {
        model.chatClosed = true
        hangUp()
    }

    fun hangUp() {
        LocalBroadcastManager.getInstance(mActivity.applicationContext)
            .sendBroadcast(BroadcastIntentHelper.buildHangUpIntent())

        // wait for the jitsi event to update the view
        // if jitsi is not running what should we do ? -> if was not running then call onHangup manually to propagate UI updates
        if (model.onGoingVideoCall) {
            return
        }
        onHangup()
    }

    fun startJitsi(
        jitsiRoom: String,
        jitsiToken: String,
        jitsiServerAddress: String,
    ) {
        val options: JitsiMeetConferenceOptions = JitsiMeetConferenceOptions.Builder()
            .setRoom("pallab-test-1")
            .setUserInfo(
                JitsiMeetUserInfo().apply {
                    displayName = "Android Test"
                    NinchatSessionManager.getInstance().userName
                })
            //.setToken(jitsiToken)
            .setServerURL(URL("https://meet.jit.si"))
            .setFeatureFlag("pip.enabled", false)
            .setFeatureFlag("add-people.enabled", false)
            .setFeatureFlag("calendar.enabled", false)
            .setFeatureFlag("chat.enabled", false)
            .setFeatureFlag("filmstrip.enabled", false)
            .setFeatureFlag("invite.enabled", false)
            .setFeatureFlag("kick-out.enabled", false)
            .setFeatureFlag("live-streaming.enabled", false)
            .setFeatureFlag("meeting-name.enabled", false)
            .setFeatureFlag("meeting-password.enabled", false)
            .setFeatureFlag("recording.enabled", false)
            .setFeatureFlag("recording.enabled", false)
            .setFeatureFlag("server-url-change.enabled", false)
            .setFeatureFlag("filmstrip.enabled", true)
            .build()

        jitsiMeetView.join(options)
        onStartVideo()
    }

    fun onNewMessage(view: View) {
        presenter.onNewMessage(
            view = view,
            hasUnreadMessage = model.onGoingVideoCall && !model.chatClosed && !model.showChatView
        )
    }

    private fun onStartVideo() {
        model.onGoingVideoCall = true
        model.showChatView = false
        model.softkeyboardVisible = false
        mActivity.ninchat_chat_root?.apply {
            hideKeyBoardForce()

            ninchat_titlebar.ninchat_titlebar_toggle_chat.visibility = View.VISIBLE
            ninchat_conference_view.visibility = View.GONE
            ninchat_p2p_video_view.visibility = View.GONE
            jitsi_frame_layout.visibility = View.VISIBLE

            // set updated layout parameter
            val (conferenceViewParams, commandViewParams) = presenter.getLayoutParams(mActivity = mActivity)
            conference_or_p2p_view_container.layoutParams = conferenceViewParams
            chat_message_list_and_editor.layoutParams = commandViewParams

            presenter.onNewMessage(
                view = ninchat_titlebar,
                hasUnreadMessage = false
            )
        }

    }

    fun onHangup() {
        model.onGoingVideoCall = false
        model.showChatView = true
        model.softkeyboardVisible = false

        mActivity.hideKeyBoardForce()
        // mActivity.jitsi_frame_layout.removeView(jitsiMeetView)
        presenter.renderInitialView(mActivity = mActivity)
        jitsiMeetView.dispose()

    }

    fun onSoftKeyboardVisibilityChanged(isVisible: Boolean) {
        model.softkeyboardVisible = isVisible
        mActivity.ninchat_chat_root?.apply {
            // set updated layout parameter
            val (conferenceViewParams, commandViewParams) = presenter.getLayoutParams(mActivity = mActivity)
            conference_or_p2p_view_container.layoutParams = conferenceViewParams
            chat_message_list_and_editor.layoutParams = commandViewParams
        }
    }


    fun onToggleChat(mActivity: NinchatChatActivity) {
        model.showChatView = !model.showChatView
        mActivity.ninchat_chat_root?.apply {
            hideKeyBoardForce()
            // set updated layout parameter
            val (conferenceViewParams, commandViewParams) = presenter.getLayoutParams(mActivity = mActivity)
            conference_or_p2p_view_container.layoutParams = conferenceViewParams
            chat_message_list_and_editor.layoutParams = commandViewParams
            if (model.showChatView) {
                // update new message icon
                onNewMessage(view = ninchat_titlebar)
            }
        }
    }

    fun handleOrientationChange(currentOrientation: Int) {
        mActivity.ninchat_chat_root?.apply {
            // hide the keyboard
            hideKeyBoardForce()
            val isLargeScreen = presenter.getScreenSize(mActivity = mActivity) == 0
            // fix the parent content view orientation
            content_view.orientation =
                if (isLargeScreen) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL

            // set updated layout parameter
            val (conferenceViewParams, commandViewParams) = presenter.getLayoutParams(mActivity = mActivity)
            conference_or_p2p_view_container.layoutParams = conferenceViewParams
            chat_message_list_and_editor.layoutParams = commandViewParams
        }
        // handleJitsiOrientation(currentOrientation = currentOrientation)
    }


    // this method needs to be refactored
    fun handleJitsiOrientation(currentOrientation: Int) {
        if (!model.onGoingVideoCall) return

        val isPortrait = currentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val confH = mActivity.conference_or_p2p_view_container.height
        val confW = mActivity.conference_or_p2p_view_container.width

        val lm = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        val realH = mActivity.content_view.height
        val realW = mActivity.content_view.width
        mActivity.jitsi_frame_layout.updateViewLayout(jitsiMeetView, lm)
        jitsiMeetView.requestLayout()
    }
}