package com.ninchat.sdk.ninchatvideointegrations.jitsi

import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.ninchatchatactivity.view.NinchatChatActivity
import com.ninchat.sdk.ninchatvideointegrations.jitsi.model.NinchatGroupCallModel
import com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter.NinchatGroupCallPresenter
import com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter.OnClickListener
import com.ninchat.sdk.utils.display.getScreenHeight
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
    private var jitsiMeetView: JitsiMeetView? = null

    init {
        jitsiMeetView = mActivity.jitsi_view
        presenter.renderInitialView(mActivity = mActivity)
        attachHandler()
    }

    private fun attachHandler() {
        mActivity.conference_join_button.setOnClickListener {
            onClickListener.onClickListener {
                presenter.onClickHandler()
            }
        }
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
            .setRoom(jitsiRoom)
            .setUserInfo(
                JitsiMeetUserInfo().apply {
                    displayName = NinchatSessionManager.getInstance().userName
                })
            .setToken(jitsiToken)
            .setServerURL(URL(jitsiServerAddress))
            //.setServerURL(URL("https://meet.jit.si"))
            .setFeatureFlag("add-people.enabled", false)
            .setFeatureFlag("android.audio-focus.disabled", false)
            .setFeatureFlag("audio-mute.enabled", true)
            .setFeatureFlag("calendar.enabled", false)
            .setFeatureFlag("call-integration.enabled", false)
            .setFeatureFlag("car-mode.enabled", false)
            .setFeatureFlag("close-captions.enabled", false)
            .setFeatureFlag("conference-timer.enabled", true)
            .setFeatureFlag("chat.enabled", false)
            .setFeatureFlag("filmstrip.enabled", true)
            .setFeatureFlag("invite.enabled", false)
            .setFeatureFlag("android.screensharing.enabled", false)
            .setFeatureFlag("speakerstats.enabled", false)
            .setFeatureFlag("kick-out.enabled", false)
            .setFeatureFlag("live-streaming.enabled", false)
            .setFeatureFlag("meeting-name.enabled", true)
            .setFeatureFlag("meeting-password.enabled", false)
            .setFeatureFlag("notifications.enabled", false)
            .setFeatureFlag("overflow-menu.enabled", true)
            .setFeatureFlag("pip.enabled", false)
            .setFeatureFlag("pip-while-screen-sharing.enabled", false)
            .setFeatureFlag("prejoinpage.enabled", true)
            .setFeatureFlag("prejoinpage.hide-display-name.enabled", true)
            .setFeatureFlag("raise-hand.enabled", false)
            .setFeatureFlag("recording.enabled", false)
            //.setFeatureFlag("resolution", 360)
            .setFeatureFlag("server-url-change.enabled", false)
            .setFeatureFlag("settings.enabled", true)
            .setFeatureFlag("tile-view.enabled", true)
            .setFeatureFlag("toolbox.alwaysVisible", false)
            .setFeatureFlag("toolbox.enabled", true)
            .setFeatureFlag("video-mute.enabled", true)
            .setFeatureFlag("video-share.enabled", false)
            .setFeatureFlag("fullscreen.enabled", false)
            .setFeatureFlag("welcomepage.enabled", false)
            .setFeatureFlag("help.enabled", false)
            .setFeatureFlag("lobby-mode.enabled", false)
            .setFeatureFlag("reactions.enabled", false)
            .setFeatureFlag("settings.profile-section.enabled", false)
            .setFeatureFlag("settings.conference-section-only-self-view.enabled", true)
            .setFeatureFlag("settings.links-section.enabled", false)
            .setFeatureFlag("settings.build-info-section.enabled", false)
            .setFeatureFlag("settings.advanced-section.enabled", false)
            .setFeatureFlag("security-options.enabled", false)
            .build()

        jitsiMeetView?.join(options)
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
            val (conferenceViewParams, commandViewParams, _) = presenter.getLayoutParams(mActivity = mActivity)
            conference_or_p2p_view_container.layoutParams = conferenceViewParams
            chat_message_list_and_editor.layoutParams = commandViewParams

            content_view.layoutParams = content_view.layoutParams.let {
                val mLayout = it as RelativeLayout.LayoutParams
                mLayout.topMargin = ninchat_titlebar.height
                mLayout.removeRule(RelativeLayout.BELOW)
                mLayout
            }

            jitsi_view.layoutParams = jitsi_view.layoutParams.let { params ->
                params.height = LinearLayout.LayoutParams.MATCH_PARENT
                params
            }

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
        presenter.renderInitialView(mActivity = mActivity)
        jitsiMeetView?.dispose()

        mActivity.ninchat_chat_root.apply {
            content_view.layoutParams = content_view.layoutParams.let {
                val mLayout = it as RelativeLayout.LayoutParams
                mLayout.topMargin = 0
                mLayout.addRule(RelativeLayout.BELOW, ninchat_titlebar.id)
                mLayout
            }
        }
    }

    fun onSoftKeyboardVisibilityChanged(isVisible: Boolean) {
        model.softkeyboardVisible = isVisible
        mActivity.ninchat_chat_root?.apply {
            // set updated layout parameter
            val (conferenceViewParams, commandViewParams, _) = presenter.getLayoutParams(mActivity = mActivity)
            conference_or_p2p_view_container.layoutParams = conferenceViewParams
            chat_message_list_and_editor.layoutParams = commandViewParams
        }
    }


    fun onToggleChat(mActivity: NinchatChatActivity) {
        model.showChatView = !model.showChatView

        mActivity.ninchat_chat_root?.apply {
            hideKeyBoardForce()
            // set updated layout parameter
            val (conferenceViewParams, commandViewParams, isLargeScreen) = presenter.getLayoutParams(
                mActivity = mActivity
            )
            conference_or_p2p_view_container.layoutParams = conferenceViewParams
            chat_message_list_and_editor.layoutParams = commandViewParams
            if (!isLargeScreen) {
                jitsi_view.layoutParams = if (model.showChatView) {
                    jitsi_view.layoutParams.let { params ->
                        params.height = mActivity.getScreenHeight()
                        params
                    }
                } else {
                    jitsi_view.layoutParams.let { params ->
                        params.height = LinearLayout.LayoutParams.MATCH_PARENT
                        params
                    }
                }
            }
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
            val (conferenceViewParams, commandViewParams, _) = presenter.getLayoutParams(mActivity = mActivity)
            conference_or_p2p_view_container.layoutParams = conferenceViewParams
            chat_message_list_and_editor.layoutParams = commandViewParams
        }
    }
}