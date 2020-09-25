package com.ninchat.sdk.helper.sessionmanager

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.client.*
import com.ninchat.sdk.R
import com.ninchat.sdk.networkdispatchers.NinchatBeginICE
import com.ninchat.sdk.networkdispatchers.NinchatOpenSession
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert

@RunWith(AndroidJUnit4::class)
class NinchatSessionManagerHelperTest {
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

    suspend fun beginIce(currentSession: Session?): ReceiveChannel<Props?> {
        val channel = Channel<Props?>()
        currentSession?.setOnEvent { params: Props, _: Payload, _: Boolean ->
            val event = params.getString("event")
            when (event) {
                "ice_begun" -> runBlocking { channel.send(params) }
                "error" -> runBlocking { channel.send(null) }
            }
        }
        NinchatBeginICE.execute(currentSession = currentSession)
        return channel
    }

    @Test
    fun `with_null_server_list`() {
        val iceServerList = NinchatSessionManagerHelper.getServers(null, true)
        Assert.assertNull("should return null for null server list object", iceServerList)
    }

    @Test
    fun `fetch_stun_servers`() = runBlocking {
        repeat(1) {
            val currentSession = createSession().receive()
            Assert.assertNotNull("current session can not be null", currentSession)
            val iceServers = beginIce(currentSession).receive()
            repeat(1) {
                val stunServers = iceServers?.getObjectArray("stun_servers")
                val servers = NinchatSessionManagerHelper.getServers(stunServers, false)
                Assert.assertNotNull("stun server list can not be empty", currentSession)
                Assert.assertTrue(servers?.size ?: 0 > 0)
                servers?.map {
                    Assert.assertNotNull("urls cannot be null or empty", !it.url.isNullOrBlank())
                }
            }
        }
    }

    @Test
    fun `fetch_turn_servers`() = runBlocking {
        repeat(1) {
            val currentSession = createSession().receive()
            Assert.assertNotNull("current session can not be null", currentSession)
            val iceServers = beginIce(currentSession).receive()
            repeat(1) {
                val stunServers = iceServers?.getObjectArray("turn_servers")
                val servers = NinchatSessionManagerHelper.getServers(stunServers, true)
                Assert.assertNotNull("turn server list can not be empty", currentSession)
                Assert.assertTrue(servers?.size ?: 0 > 0)
                servers?.map {
                    Assert.assertTrue("credentials cannot be null or empty", !it.credential.isNullOrBlank())
                    Assert.assertNotNull("username cannot be null or empty", !it.username.isNullOrBlank())
                    Assert.assertNotNull("urls cannot be null or empty", !it.url.isNullOrBlank())
                }
            }
        }
    }

    @Test
    fun `join_a_new_queue`()= runBlocking{
        repeat(1) {
            val currentSession = createSession().receive()
        }
    }

    @Test
    fun `join_queue_from_in_queue`(){

    }

    @Test
    fun `join_queue_from_user_channel_already_in_chat`(){

    }
}