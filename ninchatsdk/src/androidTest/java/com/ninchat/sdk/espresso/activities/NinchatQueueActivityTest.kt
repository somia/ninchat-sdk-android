package com.ninchat.sdk.espresso.activities

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.activities.NinchatQueueActivity
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class NinchatQueueActivityTest {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val configurationKey = appContext.getString(com.ninchat.sdk.R.string.ninchat_configuration_key)
    @Test
    fun `back_should_be_disabled`() {

    }

    @Test
    fun `get_intent_with_queue_id`() {
        val expectedQueueId = "123456"
        val intent = NinchatQueueActivity.getLaunchIntent(appContext, expectedQueueId)
        val queueId = intent.getStringExtra(NinchatQueueActivity.QUEUE_ID)

        Assert.assertEquals(expectedQueueId, queueId)
    }

    @Test
    fun `create_activity_without_ninchat_session`() {

    }

    @Test
    fun `create_activity_with_ninchat_session`() {

    }

    @Test
    fun `destroy_queue_activity`() {

    }

    @Test
    fun `activity_result_without_queue_id`() {
        val ninchatSession = NinchatSession.Builder(appContext, configurationKey).create()
        ActivityScenario.launch(NinchatQueueActivity::class.java).use { scenario ->
            scenario.moveToState(Lifecycle.State.CREATED)
        }

    }

    @Test
    fun `activity_result_with_queue_id`() {

    }

    @Test
    fun `close_activity`() {

    }

    @Test
    fun `update_queue_status`() {

    }

}