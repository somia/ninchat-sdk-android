package com.ninchat.sdk.ninchatvideointegrations.jitsi

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
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
        presenter.renderView(
            joinConferenceView = mActivity.ninchat_conference_view,
            jitsiFrameLayout = mActivity.jitsi_frame_layout
        )
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
        fullHeight: Int,
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


        Log.d("Custom Config", "$fullHeight")
        val newViewLayout = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ).also {
            it.height = fullHeight
        }
        mActivity.ninchat_chat_root.apply {
            hideKeyBoardForce()
            jitsi_frame_layout.addView(jitsiMeetView, 0, newViewLayout)
            ninchat_titlebar.ninchat_titlebar_toggle_chat.visibility = View.VISIBLE
            ninchat_conference_view.visibility = View.GONE
            jitsi_frame_layout.visibility = View.VISIBLE
            chat_message_list_and_editor.visibility = View.GONE
        }
        jitsiMeetView.join(options)
        model.onGoingVideoCall = true
        model.chatClosed = false
        model.showChatView = false
    }

    fun onNewMessage(view: View) {
        presenter.onNewMessage(
            view = view,
            hasUnreadMessage = model.onGoingVideoCall && !model.chatClosed && !model.showChatView
        )
    }

    fun onHangup() {
        mActivity.ninchat_chat_root?.apply {
            hideKeyBoardForce()
            jitsi_frame_layout.removeView(jitsiMeetView)
            ninchat_titlebar.ninchat_titlebar_toggle_chat.visibility = View.GONE

            ninchat_conference_view.apply {
                visibility = View.VISIBLE
                conference_join_button.isEnabled = model.chatClosed == false
                conference_join_button.style(
                    if (model.chatClosed) R.style.NinchatTheme_Conference_Ended else R.style.NinchatTheme_Conference_Join
                )
                layoutParams.height =
                    mActivity.resources.getDimensionPixelSize(R.dimen.ninchat_conference_join_or_leave_view_height)
            }
            jitsi_frame_layout.visibility = View.GONE

            chat_message_list_and_editor.apply {
                // should be below conference view
                val newParams = layoutParams as RelativeLayout.LayoutParams
                newParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                newParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                newParams.addRule(RelativeLayout.BELOW, R.id.conference_or_p2p_view_container)
                newParams.topMargin = 0
                layoutParams = newParams
                visibility = View.VISIBLE
            }
        }
        jitsiMeetView.dispose()
        model.onGoingVideoCall = false
        model.showChatView = true
    }

    fun onSoftKeyboardVisibilityChanged(isVisible: Boolean) {
        // 1: was video ongoing
        if (!model.onGoingVideoCall) {
            mActivity.ninchat_chat_root?.apply {
                ninchat_conference_view.layoutParams.height =
                    if (isVisible) mActivity.resources.getDimension(R.dimen.ninchat_conference_small_screen_height)
                        .toInt() else mActivity.resources.getDimension(R.dimen.ninchat_conference_join_or_leave_view_height)
                        .toInt()

                val newParams =
                    chat_message_list_and_editor.layoutParams as RelativeLayout.LayoutParams
                newParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                newParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                newParams.addRule(RelativeLayout.BELOW, R.id.conference_or_p2p_view_container)
                chat_message_list_and_editor.layoutParams = newParams

                conference_or_p2p_view_container.requestLayout()
            }
            return
        }

        // 2: if video is running but chat view hidden then don't do something
        if (!model.showChatView) {
            return
        }
        mActivity.ninchat_chat_root?.apply {
            val newParams = chat_message_list_and_editor.layoutParams as RelativeLayout.LayoutParams
            newParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            newParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            newParams.addRule(
                RelativeLayout.BELOW,
                R.id.ninchat_titlebar
            )
            newParams.topMargin =
                if (isVisible) 0 else mActivity.resources.getDimensionPixelSize(R.dimen.ninchat_conference_small_screen_height)
            chat_message_list_and_editor.layoutParams = newParams
        }
    }


    fun onToggleChat(mActivity: NinchatChatActivity) {
        model.showChatView = !model.showChatView

        mActivity.chat_message_list_and_editor.also { messageLayout ->
            if (!model.showChatView) {
                messageLayout.visibility = View.GONE
            } else {
                messageLayout.visibility = View.VISIBLE
                val params = messageLayout.layoutParams as RelativeLayout.LayoutParams
                params.height = ViewGroup.LayoutParams.MATCH_PARENT
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                params.addRule(RelativeLayout.BELOW, R.id.ninchat_titlebar)
                params.topMargin =
                    mActivity.resources.getDimensionPixelSize(R.dimen.ninchat_conference_small_screen_height)
                messageLayout.layoutParams = params

                val sessionManager = NinchatSessionManager.getInstance()
                if (mActivity.resources.getBoolean(R.bool.ninchat_chat_background_not_tiled)) {
                    messageLayout.setBackgroundResource(sessionManager.ninchatChatBackground)
                } else {
                    Misc.getNinchatChatBackground(
                        mActivity.applicationContext,
                        sessionManager.ninchatChatBackground
                    )?.let {
                        messageLayout.background = it
                    }
                }
                onNewMessage(view = mActivity.ninchat_titlebar)
            }
            messageLayout.hideKeyBoardForce()
        }
    }
}