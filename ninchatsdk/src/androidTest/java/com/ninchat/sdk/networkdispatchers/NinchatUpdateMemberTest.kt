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
class NinchatUpdateMemberTest {
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
    fun update_member_with_empty_session() = runBlocking<Unit> {
        val actionId = NinchatUpdateMember.execute(null)
        Assert.assertEquals("should return -1 when providing an empty session", -1, actionId)
    }

    @Test
    fun update_member_with_no_channel_id() = runBlocking<Unit> {
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
            val actionId = NinchatUpdateMember.execute(
                    currentSession = currentSession,
                    channelId = null)
            repeat(1) {
                val hasError = requestAudience.receive()
                Assert.assertEquals("should failed with empty channel id", true, hasError)
            }
        }
    }

    @Test
    fun update_member_with_no_user_id() = runBlocking<Unit> {
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
            val actionId = NinchatUpdateMember.execute(
                    currentSession = currentSession,
                    userId = null)
            repeat(1) {
                val hasError = requestAudience.receive()
                Assert.assertEquals("should failed with empty user id", true, hasError)
            }
        }
    }

    @Test
    fun update_member_with_wrong_user_id_and_channel_id() = runBlocking<Unit> {
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
            val actionId = NinchatUpdateMember.execute(
                    currentSession = currentSession,
                    userId = "wrong-user-id",
                    channelId = "wrong-channel-id")
            repeat(1) {
                val hasError = requestAudience.receive()
                Assert.assertEquals("should failed with wrong user id and channel id", true, hasError)
            }
        }
    }

}