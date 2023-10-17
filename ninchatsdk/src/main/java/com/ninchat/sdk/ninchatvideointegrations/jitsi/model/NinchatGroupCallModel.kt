package com.ninchat.sdk.ninchatvideointegrations.jitsi.model

import android.content.pm.ActivityInfo
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.managers.OrientationManager

data class NinchatGroupCallModel(
    var conferenceTitle: String = "",
    var conferenceButtonText: String = "",
    var conferenceDescription: String = "",
    var chatClosed: Boolean = false,
    var onGoingVideoCall: Boolean = false,
    var showChatView: Boolean = true,
    var softkeyboardVisible: Boolean = false,
    var curHeight: Int = -1,
    var curWidth: Int = -1,
    var pageContent: String = "",
    var jitsiToken: String = "",
    var jitsiRoom: String = "",
    var jitsiServerAddress: String = ""
) {

    fun parse() {
        conferenceTitle =
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getConferenceTitleText()
                ?: ""
        conferenceButtonText =
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getConferenceButtonText()
                ?: ""
        conferenceDescription =
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getConferenceDescriptionText()
                ?: ""

    }

    fun updateJitsiCredentials(jitsiRoom: String,
                               jitsiToken: String,
                               jitsiServerAddress: String,) {
        this.jitsiToken = jitsiToken
        this.jitsiRoom = jitsiRoom
        this.jitsiServerAddress = jitsiServerAddress
    }

    fun buildHTML(serverURL : String, jitsiRoom: String, jitsiToken: String, displayName: String): String {
        // remove https from the server URL

        pageContent = """
            <head>
                <meta charset="utf-8">
                <meta http-equiv="content-type" content="text/html;charset=utf-8">
            </head>
            <body style="margin:0">
            <script src="https://$serverURL/libs/external_api.min.js"></script>
            <script>
                const JITSI_CONFIG_OVERWRITE = {
                    disableInviteFunctions: true,
                    disableThirdPartyRequests: true,
                    doNotStoreRoom: true,
                    hideConferenceSubject: true,
                    prejoinConfig: {
                        enabled: true,
                        hideDisplayName: true,
                    },
                    readOnlyName: false,
                    disableDeepLinking: true,
                    disableReactions: true,
                    disableReactionsModeration: true,
                    toolbarButtons: [
                            'camera',
                        //    'chat',
                        //    'closedcaptions',
                            'desktop',
                        //    'download',
                        //    'embedmeeting',
                        //    'etherpad',
                        //    'feedback',
                            'filmstrip',
                        //    'fullscreen',
                            'hangup',
                        //    'help',
                        //    'highlight',
                        //    'invite',
                        //    'linktosalesforce',
                        //    'livestreaming',
                            'microphone',
                        //    'noisesuppression',
                        //    'participants-pane',
                        //    'profile',
                        //    'raisehand',
                        //    'recording',
                        //    'security',
                        //    'select-background',
                            'settings',
                        //    'shareaudio',
                        //    'sharedvideo',
                        //    'shortcuts',
                        //    'stats',
                            'tileview',
                            'toggle-camera',
                            'videoquality',
                        //    'whiteboard',
                      ]
                }
                const JITSI_INTERFACE_CONFIG_OVERWRITE = {
                    DISABLE_JOIN_LEAVE_NOTIFICATIONS: true,
                    HIDE_KICK_BUTTON_FOR_GUESTS: true,
                    LANG_DETECTION: true,
                    MOBILE_APP_PROMO: false,
                    SETTINGS_SECTIONS: [
                        'devices',
                    ],
                    SHOW_CHROME_EXTENSION_BANNER: false,
                    SHOW_JITSI_WATERMARK: false,
                    SHOW_PROMOTIONAL_CLOSE_PAGE: false,
                    SHOW_WATERMARK_FOR_GUESTS: false,
                    TOOLBAR_BUTTONS: [
                        'microphone',
                        'camera',
                        'desktop',
                    //    'fullscreen',
                        'fodeviceselection',
                        'hangup',
                        'videoquality',
                        'filmstrip',
                        'tileview',
                        'videobackgroundblur',
                        'download',
                    ]
                }
                let domain = "$serverURL";
                
                let options = {
                    height: '100%',
                    width: '100%',
                    configOverwrite: JITSI_CONFIG_OVERWRITE,
                    interfaceConfigOverwrite: JITSI_INTERFACE_CONFIG_OVERWRITE,
                    jwt: "$jitsiToken",
                    roomName: "$jitsiRoom",
                    parentNode: undefined,
                    userInfo: {
                        displayName: "$displayName"
                    }
                }
                let api = new JitsiMeetExternalAPI(domain, options);
                api.addEventListener('readyToClose', () => NinchatJitsiMeet.onReadyToClose() )
                api.addEventListener('videoConferenceLeft', () => NinchatJitsiMeet.onReadyToClose() )
            </script>
            </body>
            </html>
        """.trimIndent()
        return pageContent
    }
}