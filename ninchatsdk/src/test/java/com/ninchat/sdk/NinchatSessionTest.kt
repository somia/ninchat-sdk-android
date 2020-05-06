package com.ninchat.sdk

import android.app.Activity
import android.app.Application
import android.content.Context
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.*

class NinchatSessionTest {
    @Test
    fun `should build ninchat session with application context and configuration key`() {
        val mContext = mock(Application::class.java)
        val configurationKey = "configuration-key"
        NinchatSession.Builder(mContext, configurationKey)
    }

    @Test
    fun `should set ninchat session credentials`() {
        val mockBuild = mock(NinchatSession.Builder::class.java)
        `when`(mockBuild.setSessionCredentials(null)).thenReturn(mockBuild)
        mockBuild.setSessionCredentials(null)
        verify(mockBuild).setSessionCredentials(null)
    }

    @Test
    fun `should set ninchat configuration`() {
        val mockBuild = mock(NinchatSession.Builder::class.java)
        `when`(mockBuild.setSessionCredentials(null)).thenReturn(mockBuild)
        mockBuild.setSessionCredentials(null)
        verify(mockBuild).setSessionCredentials(null)
    }

    @Test
    fun `should set ninchat preferred environments`() {
        val mockBuild = mock(NinchatSession.Builder::class.java)
        `when`(mockBuild.setPreferredEnvironments(null)).thenReturn(mockBuild)
        mockBuild.setPreferredEnvironments(null)
        verify(mockBuild).setPreferredEnvironments(null)
    }

    @Test
    fun `should set ninchat event listener`() {
        val mockBuild = mock(NinchatSession.Builder::class.java)
        `when`(mockBuild.setEventListener(null)).thenReturn(mockBuild)
        mockBuild.setEventListener(null)
        verify(mockBuild).setEventListener(null)
    }

    @Test
    fun `should set ninchat log listeners`() {
        val mockBuild = mock(NinchatSession.Builder::class.java)
        `when`(mockBuild.setLogListener(null)).thenReturn(mockBuild)
        mockBuild.setLogListener(null)
        verify(mockBuild).setLogListener(null)
    }

    @Test
    fun `should set ninchat site address`() {
        val mockBuild = mock(NinchatSession::class.java)
        mockBuild.setSiteSecret("site-secret")
        verify(mockBuild).setSiteSecret("site-secret")
    }

    //todo: separate rating class and move test from here
    @Test
    fun `should be equal to GOOD rating`() {
        Assert.assertEquals(1, NinchatSession.Analytics.Rating.GOOD)
    }

    @Test
    fun `should be equal to FAIR rating`() {
        Assert.assertEquals(0, NinchatSession.Analytics.Rating.FAIR)
    }

    @Test
    fun `should be equal to POOR rating`() {
        Assert.assertEquals(-1, NinchatSession.Analytics.Rating.POOR)
    }

    @Test
    fun `should be equal to NO_ANSWER`() {
        Assert.assertEquals(-2, NinchatSession.Analytics.Rating.NO_ANSWER)
    }

    @Test
    fun `should match the Rating keys 'rating' `() {
        Assert.assertEquals("rating", NinchatSession.Analytics.Keys.RATING)
    }
}