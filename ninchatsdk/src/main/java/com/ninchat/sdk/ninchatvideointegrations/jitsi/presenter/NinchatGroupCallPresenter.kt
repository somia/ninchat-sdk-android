package com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter

import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.paris.extensions.style
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.networkdispatchers.NinchatDiscoverJitsi
import com.ninchat.sdk.ninchatvideointegrations.jitsi.model.NinchatGroupCallModel
import com.ninchat.sdk.utils.misc.Misc
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
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

    fun renderView(joinConferenceView: View, jitsiFrameLayout: FrameLayout) {
        joinConferenceView.visibility = View.VISIBLE
        jitsiFrameLayout.visibility = View.GONE
        joinConferenceView.conference_title.text = model.conferenceTitle
        joinConferenceView.conference_join_button.text = model.conferenceButtonText
        joinConferenceView.conference_description.text =
            Misc.toRichText(model.conferenceDescription, joinConferenceView.conference_description)

        joinConferenceView.conference_join_button.style(
            if (model.endConference) {
                R.style.NinchatTheme_Conference_Ended
            } else {
                R.style.NinchatTheme_Conference_Join
            }
        )
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

    fun toggleChatButtonVisibility(view: View, show: Boolean) {
        view.ninchat_titlebar_toggle_chat.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun onNewMessage(view: View, messageCount: Int) {
        view.ninchat_titlebar_toggle_chat.apply {
            setBackgroundResource(if (messageCount > 0) R.drawable.ninchat_chat_primary_button else R.drawable.ninchat_chat_secondary_button)
            setImageResource(if (messageCount > 0) R.drawable.ninchat_icon_toggle_chat_bubble_new_message else R.drawable.ninchat_icon_toggle_chat_bubble)
        }
    }

    fun onClickHandler() {
        if (model.endConference) {
            return // do nothing
        }
        loadJitsi()
    }
}