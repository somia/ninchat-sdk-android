package com.ninchat.sdk.espresso.ninchatmedia.view

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatmedia.model.NinchatFile
import com.ninchat.sdk.ninchatmedia.presenter.NinchatMediaPresenter
import com.ninchat.sdk.ninchatmedia.view.NinchatMediaActivity
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
@LargeTest
class NinchatMediaPresenterTest {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val configurationKey = appContext.getString(com.ninchat.sdk.R.string.ninchat_configuration_key)
    lateinit var activityScenario: ActivityScenario<NinchatMediaActivity>

    @After
    fun dispose() {
        try {
            activityScenario.close()
        } catch (err: Exception) {
            println(err)
        }
    }


    @Test
    fun should_create_ninchat_media_activity_with_photo_file_id() {
        NinchatSession.Builder(appContext, configurationKey).create()
        val intent = NinchatMediaPresenter.getLaunchIntent(appContext, "12345").run {
            putExtra("isDebug", true)
        }


        //add a dummy file
        val dummyFile = NinchatFile(
                "test-message_id",
                "test-id",
                "test-file-name",
                1024,
                "photo/",
                System.currentTimeMillis(),
                "test-sender",
                true
        )
        dummyFile.url = "https://ninchat.com/asset/welcome/ninchat-logo-39x44.png"
        dummyFile.urlExpiry = Date(Date().time + (1000 * 60 * 60 * 24))
        NinchatSessionManager.getInstance().ninchatState.addFile("12345", dummyFile)

        // start activity
        activityScenario = ActivityScenario.launch(intent)
        activityScenario.onActivity {
            val ninchatMediaPresenter = it.getMediaPresenter()
            Assert.assertEquals("12345", ninchatMediaPresenter.ninchatMediaModel.fileId)
            Assert.assertEquals(false, ninchatMediaPresenter.ninchatMediaModel.isDownloaded())
            Assert.assertEquals(dummyFile, ninchatMediaPresenter.ninchatMediaModel.getFile())
        }
        onView(withId(R.id.ninchat_media_name)).check(matches(withText(dummyFile.name)))
        onView(withId(R.id.ninchat_media_download)).check(matches(isDisplayed()))
        onView(withId(R.id.ninchat_media_video)).check(matches(not(isDisplayed())))
    }

    @Test
    fun should_close_activity_by_pressing_back_button() {
        NinchatSession.Builder(appContext, configurationKey).create()
        val intent = NinchatMediaPresenter.getLaunchIntent(appContext, "12345").run {
            putExtra("isDebug", true)
        }
        //add a dummy file
        val dummyFile = NinchatFile(
                "test-message_id",
                "test-id",
                "test-file-name",
                1024,
                "photo/",
                System.currentTimeMillis(),
                "test-sender",
                true
        )
        dummyFile.url = "https://ninchat.com/asset/welcome/ninchat-logo-39x44.png"
        dummyFile.urlExpiry = Date(Date().time + (1000 * 60 * 60 * 24))
        NinchatSessionManager.getInstance().ninchatState.addFile("12345", dummyFile)

        // start activity
        activityScenario = ActivityScenario.launch(intent)
        activityScenario.onActivity {
            val ninchatMediaPresenter = it.getMediaPresenter()
            Assert.assertEquals("12345", ninchatMediaPresenter.ninchatMediaModel.fileId)
            Assert.assertEquals(false, ninchatMediaPresenter.ninchatMediaModel.isDownloaded())
            Assert.assertEquals(dummyFile, ninchatMediaPresenter.ninchatMediaModel.getFile())
        }
        onView(withId(R.id.ninchat_media_name)).check(matches(withText(dummyFile.name)))
        onView(withId(R.id.ninchat_media_download)).check(matches(isDisplayed()))
        onView(withId(R.id.ninchat_media_video)).check(matches(not(isDisplayed())))
        try {
            Thread.sleep(2000)
            Espresso.pressBack()
        }catch (err: Exception){}
        finally {
            Assert.assertEquals(Lifecycle.State.DESTROYED, activityScenario.state)
        }
    }

    @Test
    fun should_close_activity_by_pressing_cross_button() {
        NinchatSession.Builder(appContext, configurationKey).create()
        val intent = NinchatMediaPresenter.getLaunchIntent(appContext, "12345").run {
            putExtra("isDebug", true)
        }
        //add a dummy file
        val dummyFile = NinchatFile(
                "test-message_id",
                "test-id",
                "test-file-name",
                1024,
                "photo/",
                System.currentTimeMillis(),
                "test-sender",
                true
        )
        dummyFile.url = "https://ninchat.com/asset/welcome/ninchat-logo-39x44.png"
        dummyFile.urlExpiry = Date(Date().time + (1000 * 60 * 60 * 24))
        NinchatSessionManager.getInstance().ninchatState.addFile("12345", dummyFile)

        // start activity
        activityScenario = ActivityScenario.launch(intent)
        activityScenario.onActivity {
            val ninchatMediaPresenter = it.getMediaPresenter()
            Assert.assertEquals("12345", ninchatMediaPresenter.ninchatMediaModel.fileId)
            Assert.assertEquals(false, ninchatMediaPresenter.ninchatMediaModel.isDownloaded())
            Assert.assertEquals(dummyFile, ninchatMediaPresenter.ninchatMediaModel.getFile())
        }
        onView(withId(R.id.ninchat_media_name)).check(matches(withText(dummyFile.name)))
        onView(withId(R.id.ninchat_media_download)).check(matches(isDisplayed()))
        onView(withId(R.id.ninchat_media_video)).check(matches(not(isDisplayed())))
        try {
            Thread.sleep(2000)
            onView(withId(R.id.close_media_button)).perform(click())
        }catch (err: Exception){}
        finally {
            Assert.assertEquals(Lifecycle.State.DESTROYED, activityScenario.state)
        }
    }

    @Test
    fun should_create_ninchat_media_activity_with_video_file_id() {
        NinchatSession.Builder(appContext, configurationKey).create()
        val intent = NinchatMediaPresenter.getLaunchIntent(appContext, "12345").run {
            putExtra("isDebug", true)
        }
        //add a dummy file
        val dummyFile = NinchatFile(
                "test-message_id",
                "test-id",
                "test-file-name",
                1024,
                "video/",
                System.currentTimeMillis(),
                "test-sender",
                true
        )
        dummyFile.url = "http://mirrors.standaloneinstaller.com/video-sample/TRA3106.3gp"
        dummyFile.urlExpiry = Date(Date().time + (1000 * 60 * 60 * 24))
        NinchatSessionManager.getInstance().ninchatState.addFile("12345", dummyFile)

        // start activity
        activityScenario = ActivityScenario.launch(intent)
        activityScenario.onActivity {
            val ninchatMediaPresenter = it.getMediaPresenter()
            Assert.assertEquals("12345", ninchatMediaPresenter.ninchatMediaModel.fileId)
            Assert.assertEquals(false, ninchatMediaPresenter.ninchatMediaModel.isDownloaded())
            Assert.assertEquals(dummyFile, ninchatMediaPresenter.ninchatMediaModel.getFile())
        }

        onView(withId(R.id.ninchat_media_name)).check(matches(withText(dummyFile.name)))
        onView(withId(R.id.ninchat_media_download)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ninchat_media_video)).check(matches(isDisplayed()))
    }

    @Test
    fun should_download_a_file_using_native_download_manager() {
        NinchatSession.Builder(appContext, configurationKey).create()
        val intent = NinchatMediaPresenter.getLaunchIntent(appContext, "12345").run {
            putExtra("isDebug", true)
        }
        //add a dummy file
        val dummyFile = NinchatFile(
                "test-message_id",
                "test-id",
                "test-file-name",
                1024,
                "photo/",
                System.currentTimeMillis(),
                "test-sender",
                true
        )
        dummyFile.url = "https://ninchat.com/asset/welcome/ninchat-logo-39x44.png"
        dummyFile.urlExpiry = Date(Date().time + (1000 * 60 * 60 * 24))
        NinchatSessionManager.getInstance().ninchatState.addFile("12345", dummyFile)

        // start activity
        activityScenario = ActivityScenario.launch(intent)
        activityScenario.onActivity {
            val ninchatMediaPresenter = it.getMediaPresenter()
            Assert.assertEquals("12345", ninchatMediaPresenter.ninchatMediaModel.fileId)
            Assert.assertEquals(false, ninchatMediaPresenter.ninchatMediaModel.isDownloaded())
            Assert.assertEquals(dummyFile, ninchatMediaPresenter.ninchatMediaModel.getFile())
        }
        onView(withId(R.id.ninchat_media_name)).check(matches(withText(dummyFile.name)))
        onView(withId(R.id.ninchat_media_download)).check(matches(isDisplayed()))
        onView(withId(R.id.ninchat_media_video)).check(matches(not(isDisplayed())))
        // try to download
        onView(withId(R.id.ninchat_media_download)).perform(click())
        onView(withId(R.id.ninchat_media_download)).check(matches(not(isDisplayed())))
    }

}