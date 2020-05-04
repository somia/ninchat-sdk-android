package com.ninchat.sdk

import org.junit.Assert.*
import org.junit.Test

class NinchatConfigurationTest {
    @Test
    fun `would give empty user name`() {
        val ninchatConfiguration = NinchatConfiguration.Builder().create()
        assertNull(ninchatConfiguration.userName)
    }
    @Test
    fun `would give a non emtpy user name`() {
        val userName = "test-user-name"
        val ninchatConfiguration = NinchatConfiguration.Builder()
                .setUserName(userName)
                .create()
        assertEquals(userName, ninchatConfiguration.userName)
    }
}