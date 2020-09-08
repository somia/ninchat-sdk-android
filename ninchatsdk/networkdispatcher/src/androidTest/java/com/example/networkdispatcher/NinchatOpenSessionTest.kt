package com.example.networkdispatcher

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.ninchat.client.Props
import com.ninchat.client.Session
import com.ninchat.client.SessionEventHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NinchatOpenSessionTest {
    @Test
    fun openNewSessionSuccess() = runBlocking<Unit> {
        val appContext = getInstrumentation().targetContext
        val siteSecret = appContext.getString(R.string.ninchat_site_secret)
        val serverAddress = appContext.getString(R.string.ninchat_server_address)
        val done = Channel<Boolean>()

        val onSession = { currentSession: Session ->
            currentSession.setOnClose {
                println("closed")
                launch {
                    done.send(false)
                }
            }
            currentSession.setOnSessionEvent(SessionEventHandler { params: Props ->
                val event: String = params.getString("event")
                println("onSessionEvent $event | ${params.toString()}")
                if (event == "session_created") {
                    launch {
                        done.send(true)
                    }
                } else if (event == "error") {
                    launch {
                        done.send(false)
                    }
                }
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
        select {
            done.onReceive { success: Boolean ->
                println("success $success")
                Assert.assertEquals("should be able to create a new session", true, success)
            }
        }
    }

    @Test
    fun openNewSessionFailed_with_wrongServerAddress() = runBlocking<Unit> {
        val appContext = getInstrumentation().targetContext
        val siteSecret = appContext.getString(R.string.ninchat_site_secret)
        val serverAddress = appContext.getString(R.string.ninchat_server_address_wrong)
        val done = Channel<Boolean>()

        val onSession = { currentSession: Session ->
            currentSession.setOnClose {
                println("closed")
                launch {
                    done.send(false)
                }
            }
            currentSession.setOnConnState() { state: String ->
                println("onConnectionStateChange $state")
                if (state == "disconnected") {
                    currentSession.close()
                }
            }
            currentSession.setOnSessionEvent(SessionEventHandler { params: Props ->
                val event: String = params.getString("event")
                println("onSessionEvent $event | ${params.toString()}")
                if (event == "session_created") {
                    launch {
                        done.send(true)
                    }
                } else if (event == "error") {
                    launch {
                        done.send(false)
                    }
                }
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
        select {
            done.onReceive { success: Boolean ->
                println("success $success")
                Assert.assertEquals("should failed to create a new session", false, success)
            }
        }
    }

    @Test
    fun openNewSessionFailed_with_wrongSiteSecret() = runBlocking<Unit> {

        val appContext = getInstrumentation().targetContext
        val siteSecret = appContext.getString(R.string.ninchat_site_secret_wrong)
        val serverAddress = appContext.getString(R.string.ninchat_server_address)
        val done = Channel<Boolean>()

        val onSession = { currentSession: Session ->
            currentSession.setOnClose {
                println("closed")
                launch {
                    done.send(false)
                }
            }
            currentSession.setOnConnState() { state: String ->
                println("onConnectionStateChange $state")
            }
            currentSession.setOnSessionEvent(SessionEventHandler { params: Props ->
                val event: String = params.getString("event")
                println("onSessionEvent $event | ${params.toString()}")
                if (event == "session_created") {
                    launch {
                        done.send(true)
                    }
                } else if (event == "error") {
                    launch {
                        done.send(false)
                    }
                }

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
        select {
            done.onReceive { success: Boolean ->
                println("success $success")
                Assert.assertEquals("should failed to create a new session", false, success)
            }
        }
    }

    @Test
    fun resumeSessionSuccess() = runBlocking<Unit> {
        val appContext = getInstrumentation().targetContext
        val siteSecret = appContext.getString(R.string.ninchat_site_secret)
        val serverAddress = appContext.getString(R.string.ninchat_server_address)
        val sessionCreated = Channel<Boolean>()
        val done = Channel<Boolean>()
        var userId: String? = null
        var userAuth: String? = null

        NinchatOpenSession.execute(
                siteSecret = siteSecret,
                serverAddress = serverAddress,
                userName = null,
                userAgent = null,
                userId = null,
                userAuth = null,
                onSession = { currentSession: Session ->
                    currentSession.setOnSessionEvent(SessionEventHandler { params: Props ->
                        val event: String = params.getString("event")
                        println("onSessionEvent $event | ${params.toString()}")
                        if (event == "session_created") {
                            userId = params.getString("user_id")
                            userAuth = params.getString("user_auth")
                            launch {
                                sessionCreated.send(true)
                            }
                        } else if (event == "error") {
                            launch {
                                sessionCreated.send(false)
                            }
                        }
                    })
                }
        )


        fun resumeSession() = launch {
            val onSession = { currentSession: Session ->
                currentSession.setOnClose {
                    println("closed")
                    launch {
                        done.send(false)
                    }
                }
                currentSession.setOnConnState() { state: String ->
                    println("onConnectionStateChange $state")
                }
                currentSession.setOnSessionEvent(SessionEventHandler { params: Props ->
                    val event: String = params.getString("event")
                    println("onSessionEvent $event | ${params.toString()}")
                    if (event == "session_created") {
                        launch {
                            done.send(true)
                        }
                    } else if (event == "error") {
                        launch {
                            done.send(false)
                        }
                    }
                })
            }

            NinchatOpenSession.execute(
                    siteSecret = siteSecret,
                    serverAddress = serverAddress,
                    userName = null,
                    userAgent = null,
                    userId = userId,
                    userAuth = userAuth,
                    onSession = onSession
            )
        }

        repeat(1) {
            val success = sessionCreated.receive()
            Assert.assertEquals("should create a new session", true, success)
            resumeSession()
        }

        repeat(1) {
            val success = done.receive()
            Assert.assertEquals("should resume a session using userId and userAuth", true, success)
        }
    }
}
