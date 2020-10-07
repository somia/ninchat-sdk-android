package com.ninchat.sdk.espresso.ninchatqueue.model

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaireHolder
import com.ninchat.sdk.ninchatqueue.model.NinchatQueueModel
import com.ninchat.sdk.ninchatqueue.presenter.NinchatQueuePresenter
import com.ninchat.sdk.ninchatqueue.view.NinchatQueueActivity
import org.junit.Assert
import org.junit.Test

class NinchatQueueModelTest {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val configurationKey = appContext.getString(com.ninchat.sdk.R.string.ninchat_configuration_key)

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

    @Test
    fun `should_update_queue_id`() {
        Thread.sleep(2000)
        val expectedQueueId = "12345"
        val ninchatQueueModel = NinchatQueueModel()
        ninchatQueueModel.queueId = expectedQueueId
        Assert.assertEquals(expectedQueueId, ninchatQueueModel.queueId)
    }

    @Test
    fun `should_be_able_to_parse_queue_related_text_from_site_config`() {
        Thread.sleep(2000)
        NinchatSession.Builder(appContext, configurationKey).create()
        NinchatSessionManager.getInstance().ninchatState.siteConfig.setConfigString(siteConfig)
        NinchatSessionManager.getInstance().ninchatState.ninchatQuestionnaire = NinchatQuestionnaireHolder(NinchatSessionManager.getInstance())

        val ninchatQueueModel = NinchatQueueModel()
        Assert.assertEquals("in queue text test", ninchatQueueModel.getInQueueMessageText())
        Assert.assertEquals("Sulje keskustelu", ninchatQueueModel.getChatCloseText())
    }

    @Test
    fun `get_intent_with_queue_id`() {
        val expectedQueueId = "123456"
        val intent = NinchatQueuePresenter.getLaunchIntentWithQueueId(appContext, expectedQueueId)
        val queueId = intent.getStringExtra(NinchatQueueModel.QUEUE_ID)
        Assert.assertEquals(expectedQueueId, queueId)
    }
}