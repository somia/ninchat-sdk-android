package com.ninchat.sdk

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class NinchatSessionManagerTest {

    lateinit var ninchatSessionManager: NinchatSessionManager
    @Before
    fun setUp() {
        ninchatSessionManager = NinchatSessionManager(null,null,null,null,null, null, null)
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
}