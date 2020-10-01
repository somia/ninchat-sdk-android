package com.ninchat.sdk.espresso.activities

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.activities.NinchatQueueActivity
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class NinchatQueueActivityTest {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val configurationKey = appContext.getString(com.ninchat.sdk.R.string.ninchat_configuration_key)

    @Test
    fun `back_should_not_finish_activity`() {
        NinchatSession.Builder(appContext, configurationKey).create()
        val scenario = ActivityScenario.launch(NinchatQueueActivity::class.java)
        scenario.onActivity {
            it.onBackPressed()
        }
        Assert.assertEquals(Lifecycle.State.CREATED, scenario.state)
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
        val activityScenario = ActivityScenario.launch(NinchatQueueActivity::class.java)
        Assert.assertEquals(Lifecycle.State.DESTROYED, activityScenario.state)
    }

    @Test
    fun `create_activity_with_ninchat_session`() {
        NinchatSession.Builder(appContext, configurationKey).create()
        val activityScenario = ActivityScenario.launch(NinchatQueueActivity::class.java)
        Assert.assertEquals(Lifecycle.State.CREATED, activityScenario.state)
        activityScenario.close()
    }

    @Test
    fun `activity_result_without_queue_id`() {
        NinchatSession.Builder(appContext, configurationKey).create()
        val activityScenario = ActivityScenario.launch(NinchatQueueActivity::class.java)
        activityScenario.onActivity {
            Assert.assertNull(it.queueId)
        }
        activityScenario.close()
    }

    @Test
    fun `activity_result_with_queue_id`() {
        NinchatSession.Builder(appContext, configurationKey).create()
        val expectedQueueId = "test-queue"
        val intent = NinchatQueueActivity.getLaunchIntent(appContext, expectedQueueId)

        val activityScenario = ActivityScenario.launch<NinchatQueueActivity>(intent)
        activityScenario.onActivity {
            Assert.assertEquals(expectedQueueId, it.queueId)
        }
        activityScenario.close()
    }


    @Test
    fun `update_queue_status_without_channel`() {

    }

    @Test
    fun `update_queue_status_with_channel`() {
        NinchatSession.Builder(appContext, configurationKey).create()
        NinchatSessionManager.getInstance().ninchatState.currentSessionState = 1 shl NinchatQuestionnaireTypeUtil.HAS_CHANNEL
        val expectedQueueId = "test-queue"
        val intent = NinchatQueueActivity.getLaunchIntent(appContext, expectedQueueId)
        val activityScenario = ActivityScenario.launch<NinchatQueueActivity>(intent)
        /*onView(withId(R.id.ninchat_queue_activity_queue_status)).check(
                matches(withText("some text"))
        )*/
        activityScenario.close()
    }

}