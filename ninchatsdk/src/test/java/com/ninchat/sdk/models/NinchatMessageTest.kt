package com.ninchat.sdk.models

import android.text.Spanned
import org.json.JSONObject
import org.junit.Test
import org.junit.Assert
import org.junit.rules.ExpectedException
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.lang.Exception
import java.util.*

class NinchatMessageTest {
    @Test
    fun `ninchat message type should not be null`() {
        Assert.assertNotNull(NinchatMessage.Type.valueOf("META"))
        Assert.assertNotNull(NinchatMessage.Type.valueOf("MESSAGE"))
        Assert.assertNotNull(NinchatMessage.Type.valueOf("WRITING"))
        Assert.assertNotNull(NinchatMessage.Type.valueOf("END"))
        Assert.assertNotNull(NinchatMessage.Type.valueOf("PADDING"))
        Assert.assertNotNull(NinchatMessage.Type.valueOf("MULTICHOICE"))
    }
    @Test(expected = IllegalArgumentException::class)
    fun `ninchat message type should throw exception for differnet enum type`() {
        Assert.assertNotNull(NinchatMessage.Type.valueOf("SOMETHING ELSE"))
    }

    @Test
    fun `should create a NinchatMessage with only TYPE and timestamp`() {
        val ninchatMessage = NinchatMessage(NinchatMessage.Type.META, 0)
        Assert.assertEquals(ninchatMessage.type, NinchatMessage.Type.META)
        Assert.assertEquals(ninchatMessage.timestamp, Date(0))
    }

    @Test
    fun `should not set data message is TYPE is WRITING`() {
        val ninchatMessage = NinchatMessage(NinchatMessage.Type.WRITING, "test data", 0)
        Assert.assertNull(ninchatMessage.message)
    }

}
