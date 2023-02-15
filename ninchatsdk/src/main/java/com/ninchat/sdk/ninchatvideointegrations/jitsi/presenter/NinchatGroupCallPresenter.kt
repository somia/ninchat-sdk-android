package com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter

import android.util.Log
import android.view.View
import com.airbnb.paris.extensions.style
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatvideointegrations.jitsi.model.NinchatGroupCallModel
import com.ninchat.sdk.utils.misc.Misc
import kotlinx.android.synthetic.main.ninchat_join_end_conference.view.*

class NinchatGroupCallPresenter(
    private val model: NinchatGroupCallModel
) {
    fun renderView(videoContainer: View) {
        videoContainer.visibility = View.VISIBLE
        videoContainer.conference_title.text = model.conferenceTitle
        videoContainer.conference_join_button.text = model.conferenceButtonText
        videoContainer.conference_description.text =
            Misc.toRichText(model.conferenceDescription, videoContainer.conference_description)

        videoContainer.conference_join_button.style(
            if (model.endConference) {
                R.style.NinchatTheme_Conference_Ended
            } else {
                R.style.NinchatTheme_Conference_Join
            }
        )
    }


    fun onClickHandler() {
        if (model.endConference) {
            return // do nothing
        }
        // todo(pallab) start initializing jitsi call
        Log.d("NinchatGroupCallPresenter", "onClickHandler: start initializing jitsi call")
    }
}