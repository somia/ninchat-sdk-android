package com.ninchat.sdk.models

import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*

class NinchatOptionTest {
    @Test
    fun `should initial NinchatOption with option JSON`() {
        val mockedJson = mock(JSONObject::class.java)
        `when`(mockedJson.getString("label")).thenReturn("test-label")
        val ninchatOption = NinchatOption(mockedJson)
        Assert.assertEquals("test-label", ninchatOption.label)
    }

    @Test
    fun `should initialize with selected false`() {
        val mockedJson = mock(JSONObject::class.java)
        val ninchatOption = NinchatOption(mockedJson)
        Assert.assertEquals(false, ninchatOption.isSelected)
    }

    @Test
    fun `should be able to toggle selected value`() {
        val mockedJson = mock(JSONObject::class.java)
        val ninchatOption = NinchatOption(mockedJson)
        Assert.assertEquals(false, ninchatOption.isSelected)
        ninchatOption.toggle()
        Assert.assertEquals(true, ninchatOption.isSelected)
    }

}