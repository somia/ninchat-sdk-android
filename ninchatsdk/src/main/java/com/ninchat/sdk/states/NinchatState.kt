package com.ninchat.sdk.states

import android.os.Build
import com.ninchat.client.Props
import com.ninchat.sdk.BuildConfig
import com.ninchat.sdk.helper.siteconfigparser.NinchatSiteConfig
import com.ninchat.sdk.models.*
import com.ninchat.sdk.ninchataudiencemetadata.NinchatAudienceMetadata
import com.ninchat.sdk.ninchatmedia.model.NinchatFile
import com.ninchat.sdk.ninchatqueuelist.model.NinchatQueue
import com.ninchat.sdk.utils.misc.Misc

class NinchatState {
    val DEFAULT_USER_AGENT = "ninchat-sdk-android/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE}; ${Build.MANUFACTURER} ${Build.MODEL})"

    var userId: String? = null
    var userName: String? = null
    var channelId: String? = null
    var siteSecret: String? = null
    var requestCode: Int = 0
    var queueId: String? = null
    var currentSessionState: Int = Misc.NEW_SESSION

    var userChannels: Props? = null
    var userQueues: Props? = null
    var audienceMetadata: NinchatAudienceMetadata? = NinchatAudienceMetadata()

    var sessionCredentials: NinchatSessionCredentials? = null

    var openQueueList: List<String>? = null

    var siteConfig: NinchatSiteConfig = NinchatSiteConfig()
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


    fun hasQuestionnaire(isPreAudienceQuestionnaire: Boolean): Boolean {
        val questionnaireList = if (isPreAudienceQuestionnaire)
            siteConfig.getPreAudienceQuestionnaire()
        else
            siteConfig.getPostAudienceQuestionnaire()
        return questionnaireList?.let { it.length() > 0 } ?: false
    }


    var members = hashMapOf<String, NinchatUser>()
    fun getMember(userId: String?): NinchatUser? {
        return members[userId]
    }

    fun addMember(userId: String, ninchatUser: NinchatUser) {
        members[userId] = ninchatUser
    }

    var queues = arrayListOf<NinchatQueue>()
    fun getQueueList(): List<NinchatQueue> {
        return queues.map { it }
    }

    fun addQueue(ninchatQueue: NinchatQueue) {
        queues.add(ninchatQueue)
    }

    var skippedReview = false

    fun dispose() {
        userId = null
        userName = null
        channelId = null
        queueId = null
        currentSessionState = Misc.NEW_SESSION
        userChannels = null
        userQueues = null
        audienceMetadata?.remove()
        audienceMetadata = null
        sessionCredentials = null
        openQueueList = null
        siteConfig = NinchatSiteConfig()
        preferredEnvironments = null
        configurationKey = null
        actionId = -1
        appDetails = null
        stunServers.clear()
        turnServers.clear()
        files.clear()
        members.clear()
        queues.clear()
        skippedReview = false
    }
}