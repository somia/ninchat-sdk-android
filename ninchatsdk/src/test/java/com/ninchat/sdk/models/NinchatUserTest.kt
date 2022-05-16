package com.ninchat.sdk.models

import com.ninchat.sdk.NinchatSessionManager
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

class NinchatUserTest {

    @Test
    fun `should able to initial a ninchat user with right parameters`() {
        val displayName = "display-name"
        val realName = "real-name"
        val avatar = "test-avater"
        val isGuest = true
        val user = NinchatUser(displayName, realName, avatar, isGuest, "", "")

        Assert.assertEquals(avatar, user.avatar)
        Assert.assertEquals(isGuest, user.isGuest)
        Assert.assertEquals(displayName, user.name)
    }

    @Test
    fun `shoud return display name getName when display name is provided`() {
        val displayName = "display-name"
        val user = NinchatUser(displayName, "real-name", "test-avater", true, "", "")
        Assert.assertEquals(displayName, user.name)
    }
}