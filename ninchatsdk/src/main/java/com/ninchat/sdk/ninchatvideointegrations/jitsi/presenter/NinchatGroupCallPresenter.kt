package com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter

import android.view.View
import android.widget.FrameLayout
import com.airbnb.paris.extensions.style
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.networkdispatchers.NinchatDiscoverJitsi
import com.ninchat.sdk.ninchatchatactivity.view.NinchatChatActivity
import com.ninchat.sdk.ninchatvideointegrations.jitsi.model.NinchatGroupCallModel
import com.ninchat.sdk.utils.misc.Misc
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
import kotlinx.android.synthetic.main.activity_ninchat_chat.*
import kotlinx.android.synthetic.main.activity_ninchat_chat.view.*
import kotlinx.android.synthetic.main.ninchat_join_end_conference.view.*
import kotlinx.android.synthetic.main.ninchat_titlebar.view.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class NinchatGroupCallPresenter(
    private val model: NinchatGroupCallModel
) {
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        NinchatSessionManager.getInstance()?.sessionError(Exception(exception))
    }

    fun initialView(mActivity: NinchatChatActivity) {
        mActivity.ninchat_chat_root?.apply {
            ninchat_conference_view.visibility = View.VISIBLE
            ninchat_p2p_video_view.visibility = View.GONE
            jitsi_frame_layout.visibility = View.GONE
            ninchat_conference_view.apply {
                conference_title.text = model.conferenceTitle
                conference_join_button.text = model.conferenceButtonText
                conference_description.text =
                    Misc.toRichText(model.conferenceDescription, conference_description)
                conference_join_button.style(
                    if (model.chatClosed) {
                        R.style.NinchatTheme_Conference_Ended
                    } else {
                        R.style.NinchatTheme_Conference_Join
                    }
                )
                visibility = View.VISIBLE
            }

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


    fun onNewMessage(view: View, hasUnreadMessage: Boolean) {
        view.ninchat_titlebar_toggle_chat.apply {
            setBackgroundResource(if (hasUnreadMessage) R.drawable.ninchat_chat_primary_button else R.drawable.ninchat_chat_secondary_button)
            setImageResource(if (hasUnreadMessage) R.drawable.ninchat_icon_toggle_chat_bubble_new_message else R.drawable.ninchat_icon_toggle_chat_bubble)
        }
    }

    fun onClickHandler() {
        if (model.chatClosed) {
            return // do nothing
        }
        loadJitsi()
    }
}