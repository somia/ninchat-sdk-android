package com.ninchat.sdk.ninchatvideointegrations.jitsi

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.ScrollView
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

    fun onChatClosed(mActivity: NinchatChatActivity) {
        hangUp(mActivity = mActivity)

        // update the UI to disable the join button
        mActivity.findViewById<ScrollView>(R.id.ninchat_conference_view)?.run {
            joinConferenceView.conference_join_button.style(
                R.style.NinchatTheme_Conference_Ended
            )
            joinConferenceView.conference_join_button.isEnabled = false
        }
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
        // jitsiFrameLayout.addView(jitsiMeetView, fullWidth, fullHeight)
    }

    fun onNewMessage(view: View, messageCount: Int) {
        presenter.onNewMessage(view = view, messageCount = messageCount)
    }

    fun hangUp(mActivity: NinchatChatActivity) {
        LocalBroadcastManager.getInstance(mActivity.applicationContext)
            .sendBroadcast(BroadcastIntentHelper.buildHangUpIntent())

        // Update UI after hangup
        onHangup(mActivity = mActivity)
    }

    fun onHangup(mActivity: NinchatChatActivity) {
        joinConferenceView.visibility = View.VISIBLE
        jitsiFrameLayout.visibility = View.GONE
        jitsiFrameLayout.removeView(jitsiMeetView)
        mActivity.findViewById<RelativeLayout>(R.id.ninchat_chat_root)?.run {
            ninchat_p2p_video_view.visibility = View.GONE
            jitsi_frame_layout.visibility = View.GONE
            ninchat_conference_view.visibility = View.VISIBLE
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
        }
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

    fun disposeJitsi(view: View) {
        joinConferenceView.visibility = View.VISIBLE
        jitsiFrameLayout.visibility = View.GONE
        jitsiFrameLayout.removeView(jitsiMeetView)
        presenter.toggleChatButtonVisibility(view = view, show = false)
        jitsiMeetView?.dispose()
    }
}