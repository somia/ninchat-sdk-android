package com.ninchat.sdk.ninchatvideointegrations.jitsi

import android.view.View
import android.webkit.*
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.adapters.NinchatMessageAdapter
import com.ninchat.sdk.ninchatchatactivity.view.NinchatChatActivity
import com.ninchat.sdk.ninchatvideointegrations.jitsi.model.NinchatGroupCallModel
import com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter.NinchatGroupCallPresenter
import com.ninchat.sdk.ninchatvideointegrations.jitsi.presenter.OnClickListener
import com.ninchat.sdk.utils.keyboard.hideKeyBoardForce
import com.ninchat.sdk.utils.misc.NinchatAdapterCallback
import com.ninchat.sdk.utils.permission.NinchatPermission.Companion.hasVideoCallPermissions
import com.ninchat.sdk.utils.permission.NinchatPermission.Companion.requestAudioVideoPermissions
import kotlinx.android.synthetic.main.activity_ninchat_chat.*
import kotlinx.android.synthetic.main.activity_ninchat_chat.view.*
import kotlinx.android.synthetic.main.ninchat_join_end_conference.*
import kotlinx.android.synthetic.main.ninchat_titlebar.view.*
import java.net.URLEncoder

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
    private val joinChatHandler = OnClickListener(intervalInMs = 2000)
    private var jitsiMeetView: WebView? = null

    init {
        jitsiMeetView = mActivity.jitsi_view
        presenter.renderInitialView(mActivity = mActivity)
        attachHandler()
    }

    private fun attachHandler() {
        mActivity.conference_join_button.setOnClickListener {
            joinChatHandler.onClickListener {
                presenter.onClickHandler()
            }
        }
        mActivity.ninchat_video_view_translucent_background.setOnClickListener {
            if (model.showChatView) {
                onToggleChat(mActivity = mActivity)
            }
        }
    }

    fun onChannelClosed() {
        model.chatClosed = true
        hangUp()
    }

    fun hangUp() {
        onHangup()
    }

    private fun startJitsi(
        jitsiRoom: String,
        jitsiToken: String,
        jitsiServerAddress: String,
    ) {

        jitsiMeetView?.settings?.javaScriptEnabled = true
        jitsiMeetView?.settings?.domStorageEnabled = true
        jitsiMeetView?.setWebChromeClient(object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        })
        jitsiMeetView?.addJavascriptInterface(object {
            @JavascriptInterface
            fun onReadyToClose() {
                mActivity.runOnUiThread {
                    onHangup()
                }

            }
        }, "NinchatJitsiMeet")
        val displayName = NinchatSessionManager.getInstance()?.userName?.let {
            URLEncoder.encode(it, "UTF-8")
        } ?: ""
        val language =
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getLanguagePreference()
                ?: "en"
        /*jitsiMeetView?.loadUrl(
            baseURL,
            model.buildHTML(
                jitsiServerAddress,
                jitsiRoom,
                jitsiToken,
                NinchatSessionManager.getInstance().userName
            ),
            "text/html",
            "UTF-8",
            null,
        )*/
        jitsiMeetView?.loadUrl("https://ninchat.com/new/jitsi-meet.html?domain=$jitsiServerAddress&roomName=$jitsiRoom&jwt=$jitsiToken&lang=$language&displayName=$displayName")
        onStartVideo()
    }

    fun updateJitsiToken(
        jitsiRoom: String,
        jitsiToken: String,
        jitsiServerAddress: String
    ) {
        model.updateJitsiCredentials(
            jitsiRoom = jitsiRoom,
            jitsiToken = jitsiToken,
            jitsiServerAddress = jitsiServerAddress
        )
    }

    fun mayBeStartJitsi() {
        if (!hasVideoCallPermissions(mActivity.applicationContext)) {
            requestAudioVideoPermissions(mActivity)
            return
        }
        startJitsi(
            jitsiRoom = model.jitsiRoom,
            jitsiToken = model.jitsiToken,
            jitsiServerAddress = model.jitsiServerAddress
        )
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
            ninchat_video_view_translucent_background.visibility = View.GONE
            jitsi_frame_layout.visibility = View.VISIBLE

            // set updated layout parameter
            val (conferenceViewParams, commandViewParams, _) = presenter.getLayoutParams(mActivity = mActivity)
            conference_or_p2p_view_container.layoutParams = conferenceViewParams
            chat_message_list_and_editor.layoutParams = commandViewParams

            jitsi_frame_layout.layoutParams = jitsi_frame_layout.layoutParams.let {
                val mLayout = it as RelativeLayout.LayoutParams
                mLayout.topMargin = ninchat_titlebar.height
                mLayout.removeRule(RelativeLayout.BELOW)
                mLayout
            }

            presenter.onNewMessage(
                view = ninchat_titlebar,
                hasUnreadMessage = false
            )
        }
    }

    fun onHangup() {
        if (model.onGoingVideoCall) {
            jitsiMeetView?.evaluateJavascript("hangUpConference();", null);
        }
        model.onGoingVideoCall = false
        model.showChatView = true
        model.softkeyboardVisible = false

        mActivity.hideKeyBoardForce()
        presenter.renderInitialView(mActivity = mActivity)
        jitsiMeetView?.loadUrl("about:blank")
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
            if (model.showChatView) {
                // update new message icon
                onNewMessage(view = ninchat_titlebar)
                NinchatSessionManager.getInstance()
                    ?.getOnInitializeMessageAdapter(object : NinchatAdapterCallback {
                        override fun onMessageAdapter(adapter: NinchatMessageAdapter) {
                            adapter.scrollToBottom(true)
                        }
                    })
            }
            ninchat_video_view_translucent_background.visibility =
                if (model.showChatView) View.VISIBLE else View.GONE
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
