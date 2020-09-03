package com.example.networkdispatcher

import com.ninchat.client.Props
import com.ninchat.client.Session
import com.ninchat.client.Strings

class NinchatOpenSession {
    companion object {
        fun execute(currentSession: Session? = null,
                    siteSecret: String? = null,
                    userName: String? = null,
                    userId: String? = null,
                    userAuth: String? = null,
                    userAgent: String,
                    serverAddress: String) {

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
            session.setHeader("User-Agent", userAgent)
            session.setAddress(serverAddress)
            session.setParams(sessionParams)
            session.open()
        }
    }
}