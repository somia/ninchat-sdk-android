package com.ninchat.sdk.models

import org.junit.Assert
import org.junit.Test

class NinchatWebRTCServerInfoTest {
    @Test
    fun `should call NinchatWebRTCServerInfo with server url only`() {
        val serverURL = "server url"
        val webRTCServerInfo = NinchatWebRTCServerInfo(serverURL)
        Assert.assertEquals(serverURL, webRTCServerInfo.url)
        Assert.assertEquals("", webRTCServerInfo.username)
        Assert.assertEquals("", webRTCServerInfo.credential)
    }

    @Test
    fun `should call NinchatWebRTCServerInfo with server url, username and credentials `() {
        val serverURL = "server url"
        val userName = "user name"
        val credentials = "credentials"
        val webRTCServerInfo = NinchatWebRTCServerInfo(serverURL, userName, credentials)
        Assert.assertEquals(serverURL, webRTCServerInfo.url)
        Assert.assertEquals(userName, webRTCServerInfo.username)
        Assert.assertEquals(credentials, webRTCServerInfo.credential)
    }

}