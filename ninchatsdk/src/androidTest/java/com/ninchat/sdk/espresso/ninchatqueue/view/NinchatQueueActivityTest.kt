package com.ninchat.sdk.espresso.ninchatqueue.view

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.ninchatqueue.presenter.NinchatQueuePresenter
import com.ninchat.sdk.ninchatqueue.view.NinchatQueueActivity
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito


@RunWith(AndroidJUnit4::class)
@LargeTest
class NinchatQueueActivityTest {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val configurationKey = appContext.getString(com.ninchat.sdk.R.string.ninchat_configuration_key)
    lateinit var activityScenario: ActivityScenario<NinchatQueueActivity>

    @After
    fun dispose() {
        try {
            activityScenario.close()
        } catch (err: Exception) {
            println(err)
        }
    }

    @Test
    fun `should_close_the_activity_with_null_session`() {
        val intent = NinchatQueuePresenter.getLaunchIntentWithQueueId(appContext, "12345")
        activityScenario = ActivityScenario.launch(intent)
        Assert.assertEquals(Lifecycle.State.DESTROYED, activityScenario.state)
    }

    @Test
    fun `create_activity_with_ninchat_session`() {
        NinchatSession.Builder(appContext, configurationKey).create()
        val intent = NinchatQueuePresenter.getLaunchIntentWithQueueId(appContext, "12345").run {
            putExtra("isDebug", true)
        }
        activityScenario = ActivityScenario.launch(intent)
        Assert.assertNotEquals(Lifecycle.State.DESTROYED, activityScenario.state)
    }

    @Test
    fun `back_should_not_destroy_the_activity`() {
        NinchatSession.Builder(appContext, configurationKey).create()
        val intent = NinchatQueuePresenter.getLaunchIntentWithQueueId(appContext, "12345").run {
            putExtra("isDebug", true)
        }
        activityScenario = ActivityScenario.launch(intent)
        activityScenario.onActivity {
            it.onBackPressed()
        }
        Assert.assertNotEquals(Lifecycle.State.DESTROYED, activityScenario.state)
    }

}