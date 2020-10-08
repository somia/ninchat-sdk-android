package com.ninchat.sdk

import android.app.Activity
import android.content.Context
import com.ninchat.client.Props
import com.ninchat.client.Session
import com.ninchat.sdk.models.NinchatSessionCredentials

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 17/08/2018.
 */
class NinchatSession {
    private var sessionManager: NinchatSessionManager
    private var siteSecret: String? = null

    data class Builder(internal val mContext: Context, internal val configurationKey: String) {

        internal var sessionCredentials: NinchatSessionCredentials? = null
        internal var configuration: NinchatConfiguration? = null
        internal var preferredEnvironments: Array<String>? = null
        internal var eventListener: NinchatSDKEventListener? = null
        internal var logListener: NinchatSDKLogListener? = null

        fun create(): NinchatSession {
            return NinchatSession(this)
        }

        fun setSessionCredentials(sessionCredentials: NinchatSessionCredentials?): Builder {
            this.sessionCredentials = sessionCredentials
            return this
        }

        fun setConfiguration(configuration: NinchatConfiguration?): Builder {
            this.configuration = configuration
            return this
        }

        fun setPreferredEnvironments(preferredEnvironments: Array<String>): Builder {
            this.preferredEnvironments = preferredEnvironments
            return this
        }

        fun setEventListener(eventListener: NinchatSDKEventListener?): Builder {
            this.eventListener = eventListener
            return this
        }

        fun setLogListener(logListener: NinchatSDKLogListener?): Builder {
            this.logListener = logListener
            return this
        }
    }

    private constructor(builder: Builder) {
        val context = builder.mContext
        val configurationKey = builder.configurationKey
        val sessionCredentials = builder.sessionCredentials
        val configuration = builder.configuration
        val preferredEnvironments = builder.preferredEnvironments
        val eventListener = builder.eventListener
        val logListener = builder.logListener
        sessionManager = NinchatSessionManager.init(context, configurationKey, sessionCredentials, configuration, preferredEnvironments, eventListener, logListener)
    }

    class Analytics {
        object Keys {
            const val RATING = "rating"
        }

        object Rating {
            const val GOOD = 1
            const val FAIR = 0
            const val POOR = -1
            const val NO_ANSWER = -2
        }
    }

    object Broadcast {
        const val CONFIGURATION_FETCHED = BuildConfig.LIBRARY_PACKAGE_NAME + ".CONFIGURATION_FETCHED"
        const val SESSION_CREATED = BuildConfig.LIBRARY_PACKAGE_NAME + ".SESSION_CREATED"
        const val QUEUES_UPDATED = BuildConfig.LIBRARY_PACKAGE_NAME + ".QUEUES_UPDATED"
        const val START_FAILED = BuildConfig.LIBRARY_PACKAGE_NAME + ".START_FAILED"
    }

    @Deprecated("Deprecated since SDK v0.6.0")
    @JvmOverloads
    constructor(
            applicationContext: Context,
            configurationKey: String,
            sessionCredentials: NinchatSessionCredentials? = null,
            configurationManager: NinchatConfiguration? = null,
            preferredEnvironments: Array<String?>? = null,
            eventListener: NinchatSDKEventListener? = null,
            logListener: NinchatSDKLogListener? = null,
    ) {
        sessionManager = NinchatSessionManager.init(applicationContext, configurationKey, sessionCredentials, configurationManager, preferredEnvironments, eventListener, logListener)
    }

    /**
     * Append information to the User-Agent string.  The string should be in
     * the form "app-name/version" or "app-name/version (more; details)".
     */
    fun setAppDetails(appDetails: String?) {
        sessionManager.ninchatState?.appDetails = appDetails
    }

    fun setServerAddress(serverAddress: String?) {
        sessionManager.ninchatState?.serverAddress = serverAddress!!
    }

    fun setSiteSecret(siteSecret: String?) {
        this.siteSecret = siteSecret
    }

    fun setAudienceMetadata(audienceMetadata: Props?) {
        sessionManager.ninchatState?.audienceMetadata = audienceMetadata
    }

    val session: Session
        get() = sessionManager.session

    @JvmOverloads
    fun start(
            activity: Activity,
            requestCode: Int? = NINCHAT_SESSION_REQUEST_CODE,
            queueId: String? = null,
    ) {
        sessionManager.start(activity, siteSecret, requestCode!!, queueId)
    }

    fun close() {
        sessionManager.close()
    }

    companion object {
        @JvmField
        val NINCHAT_SESSION_REQUEST_CODE = NinchatSession::class.java.hashCode() and 0xffff
    }
}