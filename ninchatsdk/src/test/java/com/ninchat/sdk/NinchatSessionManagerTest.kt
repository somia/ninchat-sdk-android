package com.ninchat.sdk

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.ninchat.sdk.NinchatSessionManager.init
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class NinchatSessionManagerTest {

    lateinit var ninchatSessionManager: NinchatSessionManager

    @Before
    fun setUp() {
        ninchatSessionManager = NinchatSessionManager(null, null, null, null, null, null, null)
    }

    @Test
    fun `should get empty app details`() {
        Assert.assertNull(ninchatSessionManager.appDetails)
    }

    @Test
    fun `should get app details`() {
        val appDetails = "app-details"
        ninchatSessionManager.setAppDetails(appDetails)
        Assert.assertEquals(appDetails, ninchatSessionManager.appDetails)
    }

    @Test
    fun `should get default user agent`() {
        Assert.assertEquals(NinchatSessionManager.DEFAULT_USER_AGENT, ninchatSessionManager.userAgent)
    }

    @Test
    fun `should get user agent from app details`() {
        val defaultUserAgent = NinchatSessionManager.DEFAULT_USER_AGENT
        ninchatSessionManager.appDetails = "app-details"
        Assert.assertEquals("$defaultUserAgent app-details", ninchatSessionManager.userAgent)
    }

    @Test
    fun `should get default server address`() {
        val defaultServerAddress = "api.ninchat.com"
        Assert.assertEquals(defaultServerAddress, ninchatSessionManager.serverAddress)
    }

    @Test
    fun `should get expected server address`() {
        val serverAddress = "test-api.ninchat.com"
        ninchatSessionManager.serverAddress = serverAddress
        Assert.assertEquals(serverAddress, ninchatSessionManager.serverAddress)
    }

    @Test
    fun `should return null audience metadata`() {
        Assert.assertNull(ninchatSessionManager.audienceMetadata)
    }

    @Test
    fun `should return provided audience metadata`() {
        // todo pass now - native method mocked
    }

    @Test
    fun `should return empty NinchatSessionManager instance if init is not called`() {
        Assert.assertNull(NinchatSessionManager.getInstance())
    }

    @Test
    fun `should return non empty NinchatSessionManager instance after init is called`() {
        NinchatSessionManager.init(null, null, null, null, null, null, null)
        Assert.assertNotNull(NinchatSessionManager.getInstance())
    }

    @Test
    fun `copyme_test_template`() {
        // todo implements me
    }
}