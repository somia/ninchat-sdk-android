package com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter

import android.view.View
import android.widget.FrameLayout
import com.airbnb.paris.extensions.style
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.networkdispatchers.NinchatDiscoverJitsi
import com.ninchat.sdk.ninchatvideointegrations.jitsi.model.NinchatGroupCallModel
import com.ninchat.sdk.utils.misc.Misc
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
import kotlinx.android.synthetic.main.ninchat_join_end_conference.view.*
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

    fun onClickHandler() {
        if (model.endConference) {
            return // do nothing
        }
        loadJitsi()
    }
}