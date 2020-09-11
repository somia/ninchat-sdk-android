package com.ninchat.sdk.networkdispatchers

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.client.*
import com.ninchat.sdk.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NinchatDescribeRealmQueuesTest {
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
    fun describe_realm_queue_with_empty_session() = runBlocking {
        val actionId = NinchatDescribeRealmQueues.execute(currentSession = null)
        Assert.assertEquals("should return -1 for null session", -1, actionId)
    }

    @Test
    fun describe_realm_queue_with_no_realmId() = runBlocking {
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
            val actionId = NinchatDescribeRealmQueues.execute(
                    currentSession = currentSession,
                    realmId = null,
                    audienceQueues = null)
            repeat(1) {
                val hasError = requestAudience.receive()
                Assert.assertEquals("should return error with realm not found if no realm id is provided", true, hasError)
            }
        }
    }

    @Test
    fun describe_realm_queue_with_wrong_realmId() = runBlocking {
        val sessionChannel = createSession()
        repeat(1) {
            val appContext = InstrumentationRegistry.getInstrumentation().targetContext
            val realmId = appContext.getString(R.string.ninchat_realm_id_wrong)
            val currentSession = sessionChannel.receive()
            val requestAudience = Channel<Boolean>()
            currentSession?.setOnEvent { params: Props, payload: Payload, lastReply: Boolean ->
                val event = params.getString("event")
                launch {
                    requestAudience.send(event == "error")
                }

            }
            val actionId = NinchatDescribeRealmQueues.execute(
                    currentSession = currentSession,
                    realmId = realmId,
                    audienceQueues = null)
            repeat(1) {
                val hasError = requestAudience.receive()
                Assert.assertEquals("should return error with request malformed event if wrong realm id provided", true, hasError)
            }
        }
    }

    @Test
    fun describe_realm_queue_with_no_audience_queues() = runBlocking {
        val sessionChannel = createSession()
        repeat(1) {
            val appContext = InstrumentationRegistry.getInstrumentation().targetContext
            val realmId = appContext.getString(R.string.ninchat_realm_id)
            val currentSession = sessionChannel.receive()
            val requestAudience = Channel<Boolean>()
            var noRealmQueuesFound = false
            currentSession?.setOnEvent { params: Props, payload: Payload, lastReply: Boolean ->
                val event = params.getString("event")
                val props = params.getObject("real_queues")
                if (props == null) {
                    noRealmQueuesFound = true
                }
                launch {
                    requestAudience.send(event != "error")
                }

            }
            val actionId = NinchatDescribeRealmQueues.execute(
                    currentSession = currentSession,
                    realmId = realmId,
                    audienceQueues = null)
            repeat(1) {
                val hasError = requestAudience.receive()
                Assert.assertEquals("should return true with correct realm id", true, hasError)
                Assert.assertEquals("should found empty realm queues with no audience queues", true, noRealmQueuesFound)
            }
        }
    }

    @Test
    fun describe_realm_queue_with_audience_queues() = runBlocking {
        val sessionChannel = createSession()
        repeat(1) {
            val appContext = InstrumentationRegistry.getInstrumentation().targetContext
            val realmId = appContext.getString(R.string.ninchat_realm_id)
            val audienceQueues = appContext.getString(R.string.ninchat_audience_queue)
            val currentSession = sessionChannel.receive()
            val requestAudience = Channel<Boolean>()
            var noRealmQueuesFound = false
            currentSession?.setOnEvent { params: Props, payload: Payload, lastReply: Boolean ->
                val event = params.getString("event")
                val props = params.getObject("real_queues")
                if (props == null) {
                    noRealmQueuesFound = true
                }
                launch {
                    requestAudience.send(event != "error")
                }

            }
            val actionId = NinchatDescribeRealmQueues.execute(
                    currentSession = currentSession,
                    realmId = realmId,
                    audienceQueues = mutableListOf(audienceQueues))
            repeat(1) {
                val hasError = requestAudience.receive()
                Assert.assertEquals("should return true with correct realm id", true, hasError)
                Assert.assertEquals("should found realm queues with provided audience queues", true, noRealmQueuesFound)
            }
        }
    }
}