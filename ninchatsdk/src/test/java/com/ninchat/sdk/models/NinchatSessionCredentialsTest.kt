package com.ninchat.sdk.models

import org.junit.Assert
import org.junit.Test

class NinchatSessionCredentialsTest {
    @Test
    fun `should call NinchatSessionCredentials with userid, userauth and sessionid`() {
        val userId = "test-userid"
        val userAuth = "test-userAuth"
        val sessionId = "test-sessionId"

        val sessionCredentials = NinchatSessionCredentials(userId, userAuth, sessionId)
        Assert.assertEquals(userId, sessionCredentials.userId)
        Assert.assertEquals(userAuth, sessionCredentials.userAuth)
        Assert.assertEquals(sessionId, sessionCredentials.sessionId)
    }

}