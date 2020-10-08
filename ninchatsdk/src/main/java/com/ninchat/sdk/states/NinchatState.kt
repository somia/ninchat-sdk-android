package com.ninchat.sdk.states

import android.os.Build
import com.ninchat.client.Props
import com.ninchat.sdk.BuildConfig
import com.ninchat.sdk.helper.siteconfigparser.NinchatSiteConfig
import com.ninchat.sdk.models.*
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaireHolder
import com.ninchat.sdk.ninchatmedia.model.NinchatFile
import com.ninchat.sdk.utils.misc.Misc

class NinchatState {
    val DEFAULT_USER_AGENT = "ninchat-sdk-android/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE}; ${Build.MANUFACTURER} ${Build.MODEL})"

    var userId: String? = null
    var channelId: String? = null
    var siteSecret: String? = null
    var requestCode: Int = 0
    var queueId: String? = null
    var currentSessionState: Int = Misc.NEW_SESSION

    var userChannels: Props? = null
    var userQueues: Props? = null
    var audienceMetadata: Props? = null

    var sessionCredentials: NinchatSessionCredentials? = null

    var openQueueList: List<String>? = null

    var siteConfig: NinchatSiteConfig = NinchatSiteConfig()
    var ninchatQuestionnaire: NinchatQuestionnaireHolder? = null
    var configurationKey: String? = null

    var preferredEnvironments: ArrayList<String>? = null
    fun setPreferredEnvironments(environmentList: Array<String>?) {
        preferredEnvironments = environmentList?.toCollection(ArrayList())
    }

    var appDetails: String? = null
    var serverAddress: String = "api.ninchat.com"
        get() {
            return if (field.isBlank()) {
                "api.ninchat.com"
            } else {
                field
            }
        }

    var actionId = -1L

    fun userAgent(): String {
        return if (appDetails.isNullOrEmpty()) DEFAULT_USER_AGENT else "$DEFAULT_USER_AGENT $appDetails"
    }

    var stunServers = arrayListOf<NinchatWebRTCServerInfo>()
    var turnServers = arrayListOf<NinchatWebRTCServerInfo>()
    fun addStunServer(webRTCServerInfo: NinchatWebRTCServerInfo) {
        stunServers.add(webRTCServerInfo)
    }

    fun addTurnServer(webRTCServerInfo: NinchatWebRTCServerInfo) {
        turnServers.add(webRTCServerInfo)
    }

    var files = hashMapOf<String, NinchatFile>()
    fun getFile(fileId: String?): NinchatFile? {
        return files[fileId]
    }

    fun addFile(fileId: String, ninchatFile: NinchatFile?) {
        ninchatFile?.let {
            files[fileId] = ninchatFile;
        }
    }


    var members = hashMapOf<String, NinchatUser>()
    fun getMember(userId: String?): NinchatUser? {
        return members[userId]
    }

    fun addMember(userId: String, ninchatUser: NinchatUser) {
        members[userId] = ninchatUser
    }

    var queues = arrayListOf<NinchatQueue>()
    fun addQueue(ninchatQueue: NinchatQueue) {
          queues.add(ninchatQueue)
    }

    fun dispose() {
        userId = null
        channelId = null
        queueId = null
        currentSessionState = Misc.NEW_SESSION
        userChannels = null
        userQueues = null
        audienceMetadata = null
        sessionCredentials = null
        openQueueList = null
        siteConfig = NinchatSiteConfig()
        ninchatQuestionnaire = null
        preferredEnvironments = null
        configurationKey = null
        actionId = -1
        appDetails = null
        stunServers.clear()
        turnServers.clear()
        files.clear()
        members.clear()
        queues.clear()
    }
}