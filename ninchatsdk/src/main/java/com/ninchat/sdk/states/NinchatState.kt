package com.ninchat.sdk.states

import android.os.Build
import com.ninchat.client.Props
import com.ninchat.sdk.BuildConfig
import com.ninchat.sdk.helper.siteconfigparser.NinchatSiteConfig
import com.ninchat.sdk.models.NinchatSessionCredentials
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaireHolder
import com.ninchat.sdk.utils.misc.Misc
import kotlin.collections.ArrayList

class NinchatState {
    private val DEFAULT_USER_AGENT = "ninchat-sdk-android/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE}; ${Build.MANUFACTURER} ${Build.MODEL})"

    var userId: String? = null
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
            return field ?: "api.ninchat.com"
        }

    var actionId = -1L

    fun userAgent(): String {
        return if (appDetails.isNullOrEmpty()) DEFAULT_USER_AGENT else "$DEFAULT_USER_AGENT $appDetails"
    }

    fun reset() {
        userId = null
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
    }
}