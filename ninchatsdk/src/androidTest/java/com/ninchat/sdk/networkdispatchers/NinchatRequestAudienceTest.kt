package com.ninchat.sdk.networkdispatchers

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.client.Payload
import com.ninchat.client.Props
import com.ninchat.client.Session
import com.ninchat.client.SessionEventHandler
import com.ninchat.sdk.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class NinchatRequestAudienceTest {
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
    fun request_audience_with_empty_session() = runBlocking<Unit> {
        val actionId = NinchatRequestAudience.execute(null)
        Assert.assertEquals("should return -1 when providing an empty session", -1, actionId)
    }

    @Test
    fun request_audience_with_no_queue_id() = runBlocking<Unit> {
        val sessionChannel = createSession()
        repeat(1) {
            val audienceMetadata = Props()
            val currentSession = sessionChannel.receive()
            val requestAudience = Channel<Boolean>()
            currentSession?.setOnEvent { params: Props, payload: Payload, lastReply: Boolean ->
                val event = params.getString("event")
                launch {
                    requestAudience.send(event == "error")
                }

            }
            val actionId = NinchatRequestAudience.execute(
                    currentSession = currentSession,
                    audienceMetadata = audienceMetadata)

            repeat(1) {
                val hasError = requestAudience.receive()
                Assert.assertEquals("should return error if no queue id is provided", true, hasError)
            }
        }
    }

    @Test
    fun request_audience_with_queue_id() = runBlocking<Unit> {
        val sessionChannel = createSession()
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val queueId = appContext.getString(R.string.ninchat_queue)
        repeat(1) {
            val audienceMetadata = Props()
            val currentSession = sessionChannel.receive()
            val requestAudience = Channel<Boolean>()
            currentSession?.setOnEvent { params: Props, payload: Payload, lastReply: Boolean ->
                val event = params.getString("event")
                launch {
                    requestAudience.send(event == "audience_enqueued")
                }

            }
            val actionId = NinchatRequestAudience.execute(
                    currentSession = currentSession,
                    queueId = queueId,
                    audienceMetadata = audienceMetadata)

            repeat(1) {
                val hasError = requestAudience.receive()
                Assert.assertEquals("should be in audience enqueue with provided queue id", true, hasError)
            }
        }
    }

    @Test
    fun request_audience_with_close_queue_id() = runBlocking<Unit> {
        val sessionChannel = createSession()
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val queueId = appContext.getString(R.string.ninchat_queue_closed)
        repeat(1) {
            val audienceMetadata = Props()
            val currentSession = sessionChannel.receive()
            val requestAudience = Channel<Boolean>()
            currentSession?.setOnEvent { params: Props, payload: Payload, lastReply: Boolean ->
                val event = params.getString("event")
                launch {
                    requestAudience.send(event == "error")
                }

            }
            val actionId = NinchatRequestAudience.execute(
                    currentSession = currentSession,
                    queueId = queueId,
                    audienceMetadata = audienceMetadata)

            repeat(1) {
                val hasError = requestAudience.receive()
                Assert.assertEquals("should get queue closed error for closed queue", true, hasError)
            }
        }
    }
}