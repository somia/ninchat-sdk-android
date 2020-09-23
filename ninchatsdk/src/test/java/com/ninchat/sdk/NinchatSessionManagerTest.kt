package com.ninchat.sdk

import android.app.Activity
import com.ninchat.sdk.NinchatSessionManager.init
import com.ninchat.sdk.helper.sessionmanager.SessionManagerHelper
import com.ninchat.sdk.models.NinchatFile
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class NinchatSessionManagerTest {

    lateinit var ninchatSessionManager: NinchatSessionManager

    @Before
    fun setUp() {
        ninchatSessionManager = NinchatSessionManager(null, null, null, null, null, null, null)
    }

    @Test
    fun `should get empty app details`() {
        Assert.assertNull(ninchatSessionManager.ninchatState.appDetails)
    }

    @Test
    fun `should get app details`() {
        val appDetails = "app-details"
        ninchatSessionManager.ninchatState.appDetails = appDetails
        Assert.assertEquals(appDetails, ninchatSessionManager.ninchatState.appDetails)
    }

    @Test
    fun `should get default user agent`() {
        Assert.assertEquals(NinchatSessionManager.getInstance().ninchatState.DEFAULT_USER_AGENT, ninchatSessionManager.ninchatState.userAgent())
    }

    @Test
    fun `should get user agent from app details`() {
        val defaultUserAgent = NinchatSessionManager.getInstance().ninchatState.DEFAULT_USER_AGENT
        ninchatSessionManager.ninchatState.appDetails = "app-details"
        Assert.assertEquals("$defaultUserAgent app-details", ninchatSessionManager.ninchatState.userAgent())
    }

    @Test
    fun `should get default server address`() {
        val defaultServerAddress = "api.ninchat.com"
        Assert.assertEquals(defaultServerAddress, ninchatSessionManager.ninchatState.serverAddress)
    }

    @Test
    fun `should get expected server address`() {
        val serverAddress = "test-api.ninchat.com"
        ninchatSessionManager.ninchatState.serverAddress = serverAddress
        Assert.assertEquals(serverAddress, ninchatSessionManager.ninchatState.serverAddress)
    }

    @Test
    fun `should return null audience metadata`() {
        Assert.assertNull(ninchatSessionManager.ninchatState.audienceMetadata)
    }

    @Test
    fun `should return non empty NinchatSessionManager instance after init is called`() {
        init(null, null, null, null, null, null, null)
        Assert.assertNotNull(NinchatSessionManager.getInstance())
    }

    @Test
    fun `should throw null context`() {
        Assert.assertNull(ninchatSessionManager.context)
    }

    @Test
    fun `should not be able to find a file with given fileId from files map`() {
        Assert.assertNull(ninchatSessionManager.ninchatState.getFile("testFileId"))
    }

    @Test
    fun `should not be able to find file with given fileId after setting it in the files map`() {
        val ninchatFile = mock(NinchatFile::class.java)
        ninchatSessionManager.ninchatState.files["testFileId"] = ninchatFile
        Assert.assertNotNull(ninchatSessionManager.ninchatState.getFile("testFileId"))
        Assert.assertNull(ninchatSessionManager.ninchatState.getFile("testFileIdElse"))
    }

    @Test
    fun `should return non empty ninchat queue list adapter`() {
        val mActivity = mock(Activity::class.java)
        Assert.assertNotNull(ninchatSessionManager.getNinchatQueueListAdapter(mActivity))
    }

    @Test
    fun `should return user name where user is not an agent`() {
        val mockNinchatSessionManager = mock(NinchatSessionManager::class.java)
        val mockedSessionManager = spy(mockNinchatSessionManager)
        `when`(mockedSessionManager.userName).thenReturn("test-user")
        Assert.assertEquals("test-user", mockedSessionManager.getName(false));
        verify(mockedSessionManager, times(1)).getName(false)
    }

    @Test
    fun `should not return user name name where user is an agent`() {
        val mockNinchatSessionManager = mock(NinchatSessionManager::class.java)
        val mockedSessionManager = spy(mockNinchatSessionManager)
        `when`(mockedSessionManager.userName).thenReturn("test-user")
        Assert.assertNotEquals("test-user", mockedSessionManager.getName(true));
        verify(mockedSessionManager, times(1)).getName(true)
    }

    @Test
    fun `copyme_test_template`() {
        // todo implements me
    }
}