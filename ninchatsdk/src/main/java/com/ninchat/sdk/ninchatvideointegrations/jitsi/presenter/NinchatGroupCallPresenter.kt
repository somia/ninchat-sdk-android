package com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter

import android.content.res.Configuration
import android.view.View
import android.widget.LinearLayout
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

    fun renderInitialView(mActivity: NinchatChatActivity) {
        val isLargeScreen = getScreenSize(mActivity = mActivity) == 0

        mActivity.ninchat_chat_root?.apply {
            content_view.orientation =
                if (isLargeScreen) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL

            ninchat_conference_view.visibility = View.VISIBLE
            ninchat_p2p_video_view.visibility = View.GONE
            jitsi_frame_layout.visibility = View.GONE
            ninchat_titlebar.ninchat_titlebar_toggle_chat.visibility = View.GONE

            ninchat_conference_view.apply {
                conference_title.text = model.conferenceTitle
                conference_join_button.text = model.conferenceButtonText
                conference_description.text =
                    Misc.toRichText(model.conferenceDescription, conference_description)
                conference_join_button.isEnabled = model.chatClosed == false
                conference_join_button.style(
                    if (model.chatClosed) R.style.NinchatTheme_Conference_Ended else R.style.NinchatTheme_Conference_Join
                )
            }
            // set updated layout parameter
            val (conferenceViewParams, commandViewParams, _) = getLayoutParams(mActivity = mActivity)
            conference_or_p2p_view_container.layoutParams = conferenceViewParams
            chat_message_list_and_editor.layoutParams = commandViewParams
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


    fun getLayoutParams(
        mActivity: NinchatChatActivity,
    ): Triple<LinearLayout.LayoutParams, LinearLayout.LayoutParams, Boolean> {
        val conferenceViewWeightInLandscape = if (model.onGoingVideoCall) {
            if (model.showChatView) 1.7f else 3.0f
        } else {
            1.7f
        }


        val conferenceViewWeightInPortrait = when {
            model.onGoingVideoCall -> {
                if (model.showChatView) {
                    if (model.softkeyboardVisible) 0f else 0.9f
                } else 3.0f
            }
            else -> {
                0.9f
            }
        }

        val isLargeScreen = getScreenSize(mActivity = mActivity) == 0

        val conferenceView = mActivity.conference_or_p2p_view_container.layoutParams.let {
            val params = it as LinearLayout.LayoutParams
            if (isLargeScreen) {
                params.width = 0
                params.height = LinearLayout.LayoutParams.MATCH_PARENT
                params.weight = conferenceViewWeightInLandscape
            } else {
                params.width = LinearLayout.LayoutParams.MATCH_PARENT
                params.height = 0
                params.weight = conferenceViewWeightInPortrait
            }
            params
        }
        val commandView = mActivity.chat_message_list_and_editor.layoutParams.let {
            val params = it as LinearLayout.LayoutParams
            if (isLargeScreen) {
                params.width = 0
                params.height = LinearLayout.LayoutParams.MATCH_PARENT
                params.weight = 3.0f - conferenceViewWeightInLandscape
            } else {
                params.width = LinearLayout.LayoutParams.MATCH_PARENT
                params.height = 0
                params.weight = 3.0f - conferenceViewWeightInPortrait
            }
            params
        }
        return Triple(conferenceView, commandView, isLargeScreen)
    }

    fun getScreenSize(mActivity: NinchatChatActivity): Int {
        val screenSize: Int = mActivity.resources.configuration.screenLayout and
                Configuration.SCREENLAYOUT_SIZE_MASK

        return when (screenSize) {
            Configuration.SCREENLAYOUT_SIZE_SMALL, Configuration.SCREENLAYOUT_SIZE_NORMAL -> 1
            Configuration.SCREENLAYOUT_SIZE_LARGE, Configuration.SCREENLAYOUT_SIZE_XLARGE -> 0
            else -> 1
        }

    }
}