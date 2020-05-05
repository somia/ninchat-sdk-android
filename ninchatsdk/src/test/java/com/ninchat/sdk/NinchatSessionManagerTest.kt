package com.ninchat.sdk

import android.app.Activity
import com.ninchat.sdk.NinchatSessionManager.init
import com.ninchat.sdk.models.NinchatFile
import org.junit.After
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
        Assert.assertNull(ninchatSessionManager.appDetails)
    }

    @Test
    fun `should get app details`() {
        val appDetails = "app-details"
        ninchatSessionManager.appDetails = appDetails
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
        Assert.assertNull(ninchatSessionManager.getFile("testFileId"))
    }

    @Test
    fun `should not be able to find file with given fileId after setting it in the files map`() {
        val ninchatFile = mock(NinchatFile::class.java)
        ninchatSessionManager.files["testFileId"] = ninchatFile
        Assert.assertNotNull(ninchatSessionManager.getFile("testFileId"))
        Assert.assertNull(ninchatSessionManager.getFile("testFileIdElse"))
    }

    @Test
    fun `should return non empty ninchat queue list adapter`() {
        val mActivity = mock(Activity::class.java)
        Assert.assertNotNull(ninchatSessionManager.getNinchatQueueListAdapter(mActivity))
    }

    @Test
    fun `should return user name where user is not an agent`() {
        val mockNinchatSessionManager = mock(NinchatSessionManager::class.java)
        val mockedSssionManager = spy(mockNinchatSessionManager);
        `when`(mockedSssionManager.userName).thenReturn("test-user")
        Assert.assertEquals("test-user", mockedSssionManager.getName(false));
    }

    @Test
    fun `copyme_test_template`() {
        // todo implements me
    }
}