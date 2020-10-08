package com.ninchat.sdk.espresso.ninchatqueue.view

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil
import com.ninchat.sdk.models.NinchatQueue
import com.ninchat.sdk.ninchatqueue.presenter.NinchatQueuePresenter
import com.ninchat.sdk.ninchatqueue.view.NinchatQueueActivity
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class NinchatQueueActivityTest {
    val siteConfig = """
        {
          "description": "SDK test queues",
          "default": {
            "questionnaireName": "Botti",
            "questionnaireAvatar": "http://testquestion.avatar.img",
            "postAudienceQuestionnaireStyle": "conversation",
            "inQueueText": "in queue text test",
            "translations": {
              "Join audience queue {{audienceQueue.queue_attrs.name}}": "Aloita chat",
              "Join audience queue {{audienceQueue.queue_attrs.name}} (closed)": " ",
              "Joined audience queue {{audienceQueue.queue_attrs.name}}, you are at position {{audienceQueue.queue_position}}.": "Kiitos! Ohjaan sinut nyt hoitajan chat-vastaanotolle.<br><br>Olet jonossa sijalla {{audienceQueue.queue_position}}.<br>Odota, että Terveystalon asiantuntija poimii sinut jonosta.",
              "Audience in queue {{queue}} accepted.": "Keskustelu aloitettu.",
              "Joined audience queue {{audienceQueue.queue_attrs.name}}, you are next.": "Kiitos! Ohjaan sinut nyt hoitajan chat-vastaanotolle.<br>Olet seuraavana vuorossa.<br>Odota, että Terveystalon asiantuntija poimii sinut jonosta.",
              "Enter your message": "Kirjoita viesti",
              "Conversation ended": "Keskustelu on päättynyt. Voit sulkea chatin.",
              "Skip": "Ohita",
              "You are invited to a video call": "Lääkäri kutsui sinut videokeskusteluun. Klikkaa kamera-ikonia aloittaaksesi.",
              "Video call declined": "Videokeskustelu hylätty",
              "Video call answered": "Videokeskustelu aloitettu",
              "Toggle fullscreen": "Koko ruutu -tila on/off",
              "Toggle audio": "Ääni on/off",
              "Toggle microphone": "Mykistä mikrofoni",
              "Toggle video": "Video on/off",
              "End video call": "Lopeta videokeskustelu",
              "Add an emoji": "Lisää emoji",
              "How was our customer service?": "<strong>Miten asiointisi onnistui?</strong>",
              "Next": "Jatka",
              "Close window": "Sulje ikkuna",
              "Submit": "Lähetä",
              "Close chat": "Sulje keskustelu",
              "Continue chat": "Jatka keskustelua",
              "Good": "Hyvin",
              "Okay": "Ok",
              "Poor": "Huonosti",
              "Accept": "Hyväksy",
              "Decline": "Hylkää",
              "Thank you for the conversation!": "Thank you for the conversation!",
              "You are invited to a video chat": "Sinut on kutsuttu videokeskusteluun",
              "wants to video chat with you": "haluaa aloittaa videokeskustelun"
            },
            "userAvatar": false,
            "userName": "Asiakas",
            "welcome": "welcome-default"
          }
        }
    """.trimIndent()
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val configurationKey = appContext.getString(R.string.ninchat_configuration_key)
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
        val intent = NinchatQueuePresenter.getLaunchIntentWithQueueId(appContext, "12345").run {
            putExtra("isDebug", true)
        }
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

    @Test
    fun `should_initialize_and_displayed_queue_view`() {
        NinchatSession.Builder(appContext, configurationKey).create()
        val currentQueue = NinchatQueue("1234", "test-queue").also {
            it.position = 1
            it.isClosed = false
        }
        val intent = NinchatQueuePresenter.getLaunchIntentWithQueueId(appContext, queueId = currentQueue.id).run {
            putExtra("isDebug", true)
        }
        // attach queue information
        NinchatSessionManager.getInstance().ninchatState.queues = arrayListOf(currentQueue)
        NinchatSessionManager.getInstance().ninchatState.siteConfig.setConfigString(siteConfig)

        activityScenario = ActivityScenario.launch(intent)
        onView(withId(R.id.ninchat_queue_activity_queue_status)).check(matches(isDisplayed()))
        onView(withId(R.id.ninchat_queue_activity_queue_message)).check(matches(isDisplayed()))
        onView(withId(R.id.ninchat_queue_activity_close_button)).check(matches(isDisplayed()))
    }

    @Test
    fun `should_initialize_and_hide_queue_view`() {
        NinchatSession.Builder(appContext, configurationKey).create()
        val currentQueue = NinchatQueue("1234", "test-queue").also {
            it.position = 1
            it.isClosed = false
        }
        val intent = NinchatQueuePresenter.getLaunchIntentWithQueueId(appContext, queueId = currentQueue.id).run {
            putExtra("isDebug", true)
        }

        // attach queue information
        NinchatSessionManager.getInstance().ninchatState.queues = arrayListOf(currentQueue)
        NinchatSessionManager.getInstance().ninchatState.currentSessionState = (1 shl NinchatQuestionnaireTypeUtil.HAS_CHANNEL)
        NinchatSessionManager.getInstance().ninchatState.siteConfig.setConfigString(siteConfig)

        activityScenario = ActivityScenario.launch(intent)
        onView(withId(R.id.ninchat_queue_activity_queue_status)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ninchat_queue_activity_queue_message)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ninchat_queue_activity_close_button)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `recreate_activity_should_not_crash`() {
        NinchatSession.Builder(appContext, configurationKey).create()
        val currentQueue = NinchatQueue("1234", "test-queue").also {
            it.position = 1
            it.isClosed = false
        }
        val intent = NinchatQueuePresenter.getLaunchIntentWithQueueId(appContext, queueId = currentQueue.id).run {
            putExtra("isDebug", true)
        }

        // attach queue information
        NinchatSessionManager.getInstance().ninchatState.queues = arrayListOf(currentQueue)
        NinchatSessionManager.getInstance().ninchatState.siteConfig.setConfigString(siteConfig)

        activityScenario = ActivityScenario.launch(intent)

        activityScenario.recreate()

        onView(withId(R.id.ninchat_queue_activity_queue_status)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ninchat_queue_activity_queue_message)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ninchat_queue_activity_close_button)).check(matches(not(isDisplayed())))
    }

}