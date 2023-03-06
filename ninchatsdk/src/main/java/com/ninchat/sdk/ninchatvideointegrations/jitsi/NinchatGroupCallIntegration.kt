package com.ninchat.sdk.ninchatvideointegrations.jitsi

import android.content.pm.ActivityInfo
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.updateLayoutParams
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.airbnb.paris.extensions.style
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.adapters.NinchatMessageAdapter
import com.ninchat.sdk.ninchatchatactivity.view.NinchatChatActivity
import com.ninchat.sdk.ninchatvideointegrations.jitsi.model.NinchatGroupCallModel
import com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter.NinchatGroupCallPresenter
import com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter.OnClickListener
import com.ninchat.sdk.utils.keyboard.hideKeyBoardForce
import com.ninchat.sdk.utils.misc.Misc
import com.ninchat.sdk.utils.misc.NinchatAdapterCallback
import kotlinx.android.synthetic.main.activity_ninchat_chat.*
import kotlinx.android.synthetic.main.activity_ninchat_chat.view.*
import kotlinx.android.synthetic.main.ninchat_join_end_conference.*
import kotlinx.android.synthetic.main.ninchat_join_end_conference.view.*
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
    private val model = NinchatGroupCallModel(chatClosed = chatClosed).apply {
        parse()
    }
    private val presenter = NinchatGroupCallPresenter(model = model)
    private val onClickListener = OnClickListener(intervalInMs = 2000)

    init {
        presenter.initialView(mActivity = mActivity)
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
            .setRoom("pallab-test-1")
            .setUserInfo(
                JitsiMeetUserInfo().apply {
                    displayName = "Android Test"
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


        val fullHeight = presenter.getDisplayHeight(mActivity = mActivity)
        Log.d("Custom Config", "$fullHeight")
        mActivity.jitsi_frame_layout.addView(
            jitsiMeetView,
            0,
            mActivity.jitsi_frame_layout.layoutParams.apply {
                height = fullHeight
            })
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
            ninchat_titlebar.ninchat_titlebar_toggle_chat.visibility = View.VISIBLE
            ninchat_conference_view.visibility = View.GONE
            jitsi_frame_layout.visibility = View.VISIBLE
            chat_message_list_and_editor.layoutParams =
                chat_message_list_and_editor.layoutParams.let {
                    val layoutParams = it as LinearLayout.LayoutParams
                    layoutParams.weight = 0f
                    layoutParams
                }
            conference_or_p2p_view_container.layoutParams =
                conference_or_p2p_view_container.layoutParams.let {
                    val layoutParams = it as LinearLayout.LayoutParams
                    layoutParams.weight = 3f
                    layoutParams
                }
            hideKeyBoardForce()
        }
    }

    fun onHangup() {
        model.onGoingVideoCall = false
        model.showChatView = true
        model.softkeyboardVisible = false
        mActivity.ninchat_chat_root?.apply {
            jitsi_frame_layout.removeView(jitsiMeetView)
            ninchat_titlebar.ninchat_titlebar_toggle_chat.visibility = View.GONE
            ninchat_conference_view.visibility = View.VISIBLE
            jitsi_frame_layout.visibility = View.GONE

            chat_message_list_and_editor.layoutParams =
                chat_message_list_and_editor.layoutParams.let {
                    val layoutParams = it as LinearLayout.LayoutParams
                    layoutParams.weight = 2.1f
                    layoutParams
                }
            conference_or_p2p_view_container.layoutParams =
                conference_or_p2p_view_container.layoutParams.let {
                    val layoutParams = it as LinearLayout.LayoutParams
                    layoutParams.weight = 0.9f
                    layoutParams
                }

            // update the button and style inside the conference view
            ninchat_conference_view.apply {
                conference_join_button.isEnabled = model.chatClosed == false
                conference_join_button.style(
                    if (model.chatClosed) R.style.NinchatTheme_Conference_Ended else R.style.NinchatTheme_Conference_Join
                )

            }
            hideKeyBoardForce()
        }
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
        model.currentOrientation = currentOrientation
        mActivity.ninchat_chat_root?.apply {
            // hide the keyboard
            hideKeyBoardForce()
            // fix the parent content view orientation
            content_view.orientation =
                if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL

            // set updated layout parameter
            val (conferenceViewParams, commandViewParams) = presenter.getLayoutParams(mActivity = mActivity)
            conference_or_p2p_view_container.layoutParams = conferenceViewParams
            chat_message_list_and_editor.layoutParams = commandViewParams
        }
    }


    // this method needs to be refactored
    fun handleJitsiOrientation() {
        if (!model.onGoingVideoCall) return
        val lm = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ).also {
            it.height = mActivity.content_view.height
        }
        mActivity.jitsi_frame_layout.updateViewLayout(jitsiMeetView, lm)
        // jitsiMeetView.requestLayout()
    }
}