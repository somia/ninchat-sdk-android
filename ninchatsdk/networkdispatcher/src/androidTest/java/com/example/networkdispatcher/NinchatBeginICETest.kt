package com.example.networkdispatcher

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.client.Payload
import com.ninchat.client.Props
import com.ninchat.client.Session
import com.ninchat.client.SessionEventHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class NinchatBeginICETest {
    @Test
    fun getICEWithEmptySession() = runBlocking<Unit> {
        val actionId = NinchatBeginICE.execute(null)
        Assert.assertEquals("should return -1 when providing an empty session", -1, actionId)
    }

    @Test
    fun getICEWithRightSession() = runBlocking<Unit> {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val siteSecret = appContext.getString(R.string.ninchat_site_secret)
        val serverAddress = appContext.getString(R.string.ninchat_server_address)
        val sessionCreated = Channel<Session?>()
        val iceConfigs = Channel<Props?>()

        NinchatOpenSession.execute(
                siteSecret = siteSecret,
                serverAddress = serverAddress,
                userName = null,
                userAgent = null,
                userId = null,
                userAuth = null,
                onSession = { currentSession: Session ->
                    currentSession.setOnClose {
                        println("closed")
                    }
                    currentSession.setOnSessionEvent(SessionEventHandler { params: Props ->
                        val event: String = params.getString("event")
                        println("onSessionEvent $event | ${params.toString()}")
                        when (event) {
                            "session_created" -> launch {
                                sessionCreated.send(currentSession)
                            }
                            "error" -> launch {
                                sessionCreated.send(null)
                            }
                        }
                    })
                    currentSession.setOnEvent { params: Props, _: Payload, _: Boolean ->
                        val event = params.getString("event")
                        when (event) {
                            "ice_begun" -> launch {
                                iceConfigs.send(params)
                            }
                            "error" -> launch {
                                iceConfigs.send(null)
                            }
                        }
                    }
                }
        )
        repeat(1) {
            val currentSession = sessionCreated.receive()
            Assert.assertNotNull("current session can not be null", currentSession)
            val actionId = NinchatBeginICE.execute(currentSession)
        }

        repeat(1) {
            val ice = iceConfigs.receive()
            // ignore validate ice config json payload. just check if they are present and not null
            Assert.assertNotNull("ice configurations can not be null", ice)
        }
    }

    @Test
    fun getICEWithClosedSession() = runBlocking<Unit> {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val siteSecret = appContext.getString(R.string.ninchat_site_secret)
        val serverAddress = appContext.getString(R.string.ninchat_server_address)
        val sessionCreated = Channel<Session?>()
        val iceConfigs = Channel<Props?>()

        NinchatOpenSession.execute(
                siteSecret = siteSecret,
                serverAddress = serverAddress,
                userName = null,
                userAgent = null,
                userId = null,
                userAuth = null,
                onSession = { currentSession: Session ->
                    currentSession.setOnClose {
                        println("closed")
                        launch {
                            sessionCreated.send(currentSession)
                        }
                    }
                    currentSession.setOnSessionEvent(SessionEventHandler { params: Props ->
                        val event: String = params.getString("event")
                        println("onSessionEvent $event | ${params.toString()}")
                        when (event) {
                            "session_created" -> launch {
                                delay(1500)
                                currentSession.close()
                            }
                            "error" -> launch {
                                sessionCreated.send(null)
                            }
                        }
                    })
                    currentSession.setOnEvent { params: Props, _: Payload, _: Boolean ->
                        val event = params.getString("event")
                        when (event) {
                            "ice_begun" -> launch {
                                iceConfigs.send(params)
                            }
                            "error" -> launch {
                                iceConfigs.send(null)
                            }
                        }
                    }
                }
        )
        repeat(1) {
            val currentSession = sessionCreated.receive()
            Assert.assertNotNull("current session can not be null", currentSession)
            val actionId = NinchatBeginICE.execute(currentSession)
        }

        repeat(1) {
            var ice: Props? = null
            try {
                withTimeout(1500) {
                    ice = iceConfigs.receive()
                }
            } catch (e: Exception) {
                Assert.assertNull("ice configurations will be null for closed session", ice)
            }
        }
    }
}