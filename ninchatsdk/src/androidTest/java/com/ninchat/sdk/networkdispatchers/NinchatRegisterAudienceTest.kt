package com.ninchat.sdk.networkdispatchers

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.client.Payload
import com.ninchat.client.Props
import com.ninchat.client.Session
import com.ninchat.client.Strings
import com.ninchat.sdk.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NinchatRegisterAudienceTest {
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
    fun with_no_session() = runBlocking {
        val actionId = NinchatRegisterAudience.execute(
                currentSession = null)
        Assert.assertEquals("should return -1 when providing an empty session", -1, actionId)
    }

    @Test
    fun with_no_queueId() = runBlocking {
        val sessionChannel = createSession()
        repeat(1) {
            val audienceMetadata = Props()
            val currentSession = sessionChannel.receive()
            val actionId = NinchatRegisterAudience.execute(
                    currentSession = currentSession,
                    queueId = null,
                    audienceMetadata = audienceMetadata)
            Assert.assertEquals("should return -1 when no queue id is provided", -1, actionId)
        }
    }

    @Test
    fun with_no_audienceMetadata() = runBlocking {
        val sessionChannel = createSession()
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val queueId = appContext.getString(R.string.ninchat_queue)
        repeat(1) {
            val currentSession = sessionChannel.receive()
            val actionId = NinchatRegisterAudience.execute(
                    currentSession = currentSession,
                    queueId = queueId,
                    audienceMetadata = null)
            Assert.assertEquals("should return -1 when audience metadata is null", -1, actionId)
        }
    }

    @Test
    fun with_queueId_and_empty_audience_metadata() = runBlocking {
        val sessionChannel = createSession()
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val queueId = appContext.getString(R.string.ninchat_queue)
        repeat(1) {
            val audienceMetadata = Props()
            val currentSession = sessionChannel.receive()
            val audienceRegistered = Channel<Boolean>()
            currentSession?.setOnEvent { params: Props, payload: Payload, lastReply: Boolean ->
                val event = params.getString("event")
                launch {
                    audienceRegistered.send(event == "audience_registered")
                }
            }
            val actionId = NinchatRegisterAudience.execute(
                    currentSession = currentSession,
                    queueId = queueId,
                    audienceMetadata = audienceMetadata)
            Assert.assertNotEquals("should to send empty audience metadata", -1, actionId)
            repeat(1) {
                val isRegistered = audienceRegistered.receive()
                Assert.assertEquals("should able to received audience_registered callback", true, isRegistered)
            }
        }
    }

    @Test
    fun with_wrong_queueId_and_empty_audience_metadata() = runBlocking {
        val sessionChannel = createSession()
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val queueId = appContext.getString(R.string.ninchat_queue) + "wrong-queue"
        repeat(1) {
            val audienceMetadata = Props()
            val currentSession = sessionChannel.receive()
            val audienceRegistered = Channel<Boolean>()
            currentSession?.setOnEvent { params: Props, payload: Payload, lastReply: Boolean ->
                val event = params.getString("event")
                launch {
                    audienceRegistered.send(event == "audience_registered")
                }
            }
            val actionId = NinchatRegisterAudience.execute(
                    currentSession = currentSession,
                    queueId = queueId,
                    audienceMetadata = audienceMetadata)
            Assert.assertNotEquals("should to send empty audience metadata", -1, actionId)
            repeat(1) {
                val isRegistered = audienceRegistered.receive()
                Assert.assertEquals("should failed to send audience register request for wrong queue", false, isRegistered)
            }
        }
    }

    @Test
    fun with_queueId_and_audience_metadata() = runBlocking {
        val sessionChannel = createSession()
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val queueId = appContext.getString(R.string.ninchat_queue)
        repeat(1) {
            val audienceMetadata = Props()

            // sample audience metadata payload for audience register
            val answers = Props()
            answers.setString("key1", "value1")
            answers.setString("key2", "value2")
            val tags = Strings()
            tags.append("tag1")
            tags.append("tag2")
            answers.setStringArray("tags", tags)
            audienceMetadata.setObject("pre_answers", answers)

            val currentSession = sessionChannel.receive()
            val audienceRegistered = Channel<Boolean>()
            currentSession?.setOnEvent { params: Props, payload: Payload, lastReply: Boolean ->
                val event = params.getString("event")
                launch {
                    audienceRegistered.send(event == "audience_registered")
                }
            }
            val actionId = NinchatRegisterAudience.execute(
                    currentSession = currentSession,
                    queueId = queueId,
                    audienceMetadata = audienceMetadata)
            Assert.assertNotEquals("should to send empty audience metadata", -1, actionId)
            repeat(1) {
                val isRegistered = audienceRegistered.receive()
                Assert.assertEquals("should send audience register request", true, isRegistered)
            }
        }
    }
}