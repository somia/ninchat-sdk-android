package com.ninchat.sdk.espresso.ninchatqueue.presenter

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil
import com.ninchat.sdk.ninchatqueue.presenter.NinchatQueuePresenter
import com.ninchat.sdk.ninchatqueue.view.NinchatQueueActivity
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
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

    }

    @Test
    fun `update_queue_id_with_out_queue_id`() {

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
    }

    @Test
    fun `get_intent_with_queue_id`() {
    }

}