package com.ninchat.sdk.espresso.ninchatqueue.presenter

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.sdk.ninchatqueue.model.NinchatQueueModel
import com.ninchat.sdk.ninchatqueue.presenter.NinchatQueuePresenter
import com.ninchat.sdk.ninchatqueue.view.NinchatQueueActivity
import com.ninchat.sdk.utils.misc.Parameter
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class NinchatQueuePresenterTest {
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
    fun `should_return_empty_session`() {
        val queuePresenter = NinchatQueuePresenter(NinchatQueueModel(), null, appContext)
        Assert.assertEquals(false, queuePresenter.hasSession())
    }

    @Test
    fun `update_queue_view_without_channel`() {
        // need starting queue activity
    }

    @Test
    fun `update_queue_view_with_channel`() {
        // need starting queue activity
    }

    @Test
    fun `update_queue_id_with_queue_id`() {
        val intent = NinchatQueuePresenter.getLaunchIntentWithQueueId(appContext, "12345").run {
            putExtra("isDebug", true)
        }

        val queuePresenter = NinchatQueuePresenter(NinchatQueueModel(), null, appContext)
        queuePresenter.updateQueueId(intent)
        Assert.assertEquals("12345", queuePresenter.ninchatQueueModel.queueId)
    }

    @Test
    fun `update_queue_id_with_out_queue_id`() {
        val queuePresenter = NinchatQueuePresenter(NinchatQueueModel(), null, appContext)
        queuePresenter.updateQueueId(null)
        Assert.assertNull(queuePresenter.ninchatQueueModel.queueId)
    }

    @Test
    fun `show_queue_animation`() {
        // omit test since it blocks espress test
    }

    @Test
    fun `subscribe_broadcaster`() {
    }

    @Test
    fun `un_subscribe_broadcaster`() {
    }

    @Test
    fun `get_intent_for_chat_activity`() {
        val intent = NinchatQueuePresenter.getLaunchIntentForChatActivity(
                appContext,
                false
        )
        Assert.assertEquals(false, intent.getBooleanExtra(Parameter.CHAT_IS_CLOSED, false))
    }

    @Test
    fun `get_intent_with_queue_id`() {
        val intent = NinchatQueuePresenter.getLaunchIntentWithQueueId(
                appContext,
                "1234"
        )
        Assert.assertEquals("1234", intent.getStringExtra(NinchatQueueModel.QUEUE_ID))
    }

}