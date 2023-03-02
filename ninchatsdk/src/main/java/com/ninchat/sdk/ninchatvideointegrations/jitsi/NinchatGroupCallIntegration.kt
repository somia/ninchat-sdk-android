package com.ninchat.sdk.ninchatvideointegrations.jitsi

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.airbnb.paris.extensions.style
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatchatactivity.view.NinchatChatActivity
import com.ninchat.sdk.ninchatvideointegrations.jitsi.model.NinchatGroupCallModel
import com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter.NinchatGroupCallPresenter
import com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter.OnClickListener
import kotlinx.android.synthetic.main.activity_ninchat_chat.view.*
import kotlinx.android.synthetic.main.ninchat_join_end_conference.view.*
import org.jitsi.meet.sdk.BroadcastIntentHelper
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import org.jitsi.meet.sdk.JitsiMeetView
import java.net.URL

class NinchatGroupCallIntegration(
    private val joinConferenceView: View,
    private val jitsiFrameLayout: FrameLayout,
    private val jitsiMeetView: JitsiMeetView,
    private val mActivity: NinchatChatActivity,
    chatClosed: Boolean = false,
) {
    private val model = NinchatGroupCallModel(endConference = chatClosed).apply {
        parse()
    }
    private val presenter = NinchatGroupCallPresenter(model = model)
    private val onClickListener = OnClickListener(intervalInMs = 2000)

    init {
        presenter.renderView(
            joinConferenceView = joinConferenceView,
            jitsiFrameLayout = jitsiFrameLayout
        )
        attachHandler()
    }

    private fun attachHandler() {
        joinConferenceView.conference_join_button.setOnClickListener {
            onClickListener.onClickListener {
                presenter.onClickHandler()
            }
        }
    }

    fun updateView(chatClosed: Boolean) {
        model.update(endConference = chatClosed)
        presenter.renderView(
            joinConferenceView = joinConferenceView,
            jitsiFrameLayout = jitsiFrameLayout
        )
    }

    fun onChannelClosed() {
        model.endConference = true
        hangUp()
    }

    fun hangUp() {
        LocalBroadcastManager.getInstance(mActivity.applicationContext)
            .sendBroadcast(BroadcastIntentHelper.buildHangUpIntent())

        // wait for the jitsi event to update the view
        // if jitsi is not running what should we do ? -> if was not running then call onHangup manually to propagate UI updates
        if (model.wasRunning) {
            return
        }
        onHangup()
    }

    fun startJitsi(
        jitsiRoom: String,
        jitsiToken: String,
        jitsiServerAddress: String,
        fullHeight: Int,
        fullWidth: Int,
        view: View
    ) {
        Log.d("JitsiMeet", "jitsiMeetServerAddress: $jitsiServerAddress $jitsiRoom $jitsiToken ")
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
            .build()


        presenter.toggleChatButtonVisibility(view = view, show = true)
        joinConferenceView.visibility = View.GONE
        jitsiFrameLayout.visibility = View.VISIBLE
        Log.d("Custom Config", "$fullWidth $fullHeight")

        jitsiMeetView.join(options)
        val lm = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ).also {
            it.height = fullHeight
        }
        jitsiFrameLayout.addView(jitsiMeetView, 0, lm)
        model.wasRunning = true
        // jitsiFrameLayout.addView(jitsiMeetView, fullWidth, fullHeight)
    }

    fun onNewMessage(view: View, messageCount: Int) {
        presenter.onNewMessage(view = view, messageCount = messageCount)
    }

    fun onHangup() {
        mActivity.findViewById<RelativeLayout>(R.id.ninchat_chat_root)?.apply {
            jitsi_frame_layout.removeView(jitsiMeetView)

            ninchat_p2p_video_view.visibility = View.GONE
            jitsi_frame_layout.visibility = View.GONE
            ninchat_titlebar.findViewById<ImageView>(R.id.ninchat_titlebar_toggle_chat)?.run {
                visibility = View.GONE
            }
            chat_message_list_and_editor.run {
                // should be below conference view
                val params = layoutParams as RelativeLayout.LayoutParams
                params.height = ViewGroup.LayoutParams.MATCH_PARENT
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                params.addRule(RelativeLayout.BELOW, R.id.conference_or_p2p_view_container)
                layoutParams = params

                visibility = View.VISIBLE
            }
            ninchat_conference_view.apply {
                visibility = View.VISIBLE
                conference_join_button.isEnabled = model.endConference == false
                conference_join_button.style(
                    if (model.endConference) R.style.NinchatTheme_Conference_Ended else R.style.NinchatTheme_Conference_Join
                )
            }
        }
        jitsiMeetView.dispose()
        model.wasRunning = false
    }

    fun updateLayout(fullWidth: Int, fullHeight: Int) {

        val lm = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ).also {
            it.height = fullHeight
        }
        jitsiFrameLayout.updateViewLayout(jitsiMeetView, lm)
        jitsiMeetView.requestLayout()
        //jitsiFrameLayout.addView(jitsiMeetView, 0, lm)
    }

    fun onSoftKeyboardVisibilityChanged(showChatView: Boolean, isVisible: Boolean) {
        // 1: was video ongoing
        if (!model.wasRunning) {
            mActivity.findViewById<RelativeLayout>(R.id.ninchat_chat_root)?.apply {
                ninchat_conference_view.layoutParams.height =
                    if (isVisible) mActivity.resources.getDimension(R.dimen.ninchat_conference_small_screen_height)
                        .toInt() else mActivity.resources.getDimension(R.dimen.ninchat_conference_join_or_leave_view_height)
                        .toInt()
                ninchat_conference_view.requestLayout()

                val params =
                    chat_message_list_and_editor.layoutParams as RelativeLayout.LayoutParams
                params.height = ViewGroup.LayoutParams.MATCH_PARENT
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                params.addRule(RelativeLayout.BELOW, R.id.conference_or_p2p_view_container)
                chat_message_list_and_editor.layoutParams = params

            }
            return
        }

        // 2: if video is running but chat view hidden then don't do something
        if (!showChatView) {
            return
        }
        mActivity.findViewById<RelativeLayout>(R.id.ninchat_chat_root)?.apply {
            val params = chat_message_list_and_editor.layoutParams as RelativeLayout.LayoutParams
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT
            params.addRule(
                RelativeLayout.BELOW,
                if (isVisible) R.id.ninchat_titlebar else R.id.ninchat_titlebar
            )
            params.topMargin =
                if(isVisible) 0 else mActivity.resources.getDimensionPixelSize(R.dimen.ninchat_conference_small_screen_height)
            chat_message_list_and_editor.layoutParams = params

        }
    }
}