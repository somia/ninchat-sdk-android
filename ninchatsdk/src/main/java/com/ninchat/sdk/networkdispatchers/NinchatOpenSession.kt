package com.ninchat.sdk.networkdispatchers

import android.os.Build
import com.ninchat.client.Props
import com.ninchat.client.Session
import com.ninchat.client.Strings
import com.ninchat.sdk.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext

class NinchatOpenSession {
    companion object {
        val defaultUserAgent = "ninchat-sdk-android/" + BuildConfig.VERSION_NAME + " (Android " + Build.VERSION.RELEASE + "; " + Build.MANUFACTURER + " " + Build.MODEL + ")"
        suspend fun execute(
                siteSecret: String? = null,
                userName: String? = null,
                userId: String? = null,
                userAuth: String? = null,
                userAgent: String? = null,
                onSession: ((mSession: Session) -> Unit)? = null,
                serverAddress: String) = withContext(Dispatchers.IO) {

            val sessionParams = Props()
            siteSecret?.let {
                sessionParams.setString("site_secret", siteSecret)
            }
            userName?.let {
                val attrs = Props()
                attrs.setString("name", userName)
                sessionParams.setObject("user_attrs", attrs)
            }

            val messageTypes = Strings()
            messageTypes.append("ninchat.com/*")
            sessionParams.setStringArray("message_types", messageTypes)

            // Session persistence. If there is already a user id and user auth
            userId?.let {
                sessionParams.setString("user_id", userId)
            }
            userAuth?.let {
                sessionParams.setString("user_auth", userAuth)
            }
            val session = Session()
            session.setHeader("User-Agent", userAgent ?: defaultUserAgent)
            session.setAddress(serverAddress)
            session.setParams(sessionParams)
            // send channel
            onSession?.let {
                onSession(session)
            }
            session.open()
        }
    }
}