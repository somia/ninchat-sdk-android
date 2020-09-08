package com.example.networkdispatcher

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.ninchat.client.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class NinchatOpenSessionTest {
    @Test
    fun openNewSession() = runBlocking<Unit> {
        val appContext = getInstrumentation().targetContext
        val siteSecret = appContext.getString(R.string.ninchat_site_secret)
        val serverAddress = appContext.getString(R.string.ninchat_server_address)

        val onSession = { currentSession: Session ->
            println("got session")
            currentSession.setOnClose {
                println("closed")
            }
            currentSession.setOnConnState { state: String ->
                println("set on connection state $state")
            }
            currentSession.setOnLog { msg: String ->
                println("set on log $msg")
            }
            currentSession.setOnSessionEvent(SessionEventHandler { params: Props ->
                println("onSessionEvent ${params.toString()}")
            })
        }
        NinchatOpenSession.execute(
                siteSecret = siteSecret,
                serverAddress = serverAddress,
                userName = null,
                userId = null,
                userAgent = null,
                userAuth = null,
                onSession = onSession
        )

    }
}
