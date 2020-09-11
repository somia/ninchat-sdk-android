package com.ninchat.sdk.networkdispatchers

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.client.Payload
import com.ninchat.client.Props
import com.ninchat.client.Session
import com.ninchat.sdk.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class NinchatPartChannelTest {
    suspend fun createSession(): ReceiveChannel<Session?> {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val siteSecret = appContext.getString(R.string.ninchat_site_secret)
        val serverAddress = appContext.getString(R.string.ninchat_server_address)
        val channel = Channel<Session?>()
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
                        runBlocking {
                            channel.send(null)
                        }
                    }
                    currentSession.setOnSessionEvent { params: Props ->
                        val event: String = params.getString("event")
                        println("onSessionEvent $event | ${params.toString()}")
                        when (event) {
                            "session_created" -> {
                                runBlocking {
                                    channel.send(currentSession)
                                }
                            }
                            "error" -> {
                                runBlocking {
                                    channel.send(null)
                                }
                            }

                        }
                    }
                }
        )
        return channel
    }

    @Test
    fun part_channel_with_empty_session() = runBlocking<Unit> {
        val actionId = NinchatPartChannel.execute(null)
        Assert.assertEquals("should return -1 when providing an empty session", -1, actionId)
    }

    @Test
    fun part_channel_with_no_channel_id() = runBlocking<Unit> {
        val sessionChannel = createSession()
        repeat(1) {
            val currentSession = sessionChannel.receive()
            val requestAudience = Channel<Boolean>()
            currentSession?.setOnEvent { params: Props, payload: Payload, lastReply: Boolean ->
                val event = params.getString("event")
                launch {
                    requestAudience.send(event == "error")
                }

            }
            val actionId = NinchatPartChannel.execute(
                    currentSession = currentSession,
                    channelId = null)
            repeat(1) {
                val hasError = requestAudience.receive()
                Assert.assertEquals("should failed to part channel with empty channel id", true, hasError)
            }
        }
    }

    @Test
    fun part_channel_with_no_wrong_channel_id() = runBlocking<Unit> {
        val sessionChannel = createSession()
        repeat(1) {
            val currentSession = sessionChannel.receive()
            val requestAudience = Channel<Boolean>()
            currentSession?.setOnEvent { params: Props, payload: Payload, lastReply: Boolean ->
                val event = params.getString("event")
                launch {
                    requestAudience.send(event == "error")
                }

            }
            val actionId = NinchatPartChannel.execute(
                    currentSession = currentSession,
                    channelId = "wrong-channel-id")
            repeat(1) {
                val hasError = requestAudience.receive()
                Assert.assertEquals("should failed to part channel with empty channel id", true, hasError)
            }
        }
    }
}