package com.ninchat.sdk.espresso.ninchatqueuelist.presenter

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatqueuelist.model.NinchatQueue
import com.ninchat.sdk.ninchatqueuelist.presenter.NinchatQueueListPresenter
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class NinchatQueueListPresenterTest {
    val siteConfig = """
        {
          "description": "SDK test queues",
          "default": {
            "questionnaireName": "Botti",
            "questionnaireAvatar": "http://testquestion.avatar.img",
            "preAudienceQuestionnaireStyle": "conversation",
            "postAudienceQuestionnaireStyle": "conversation",
            "preAudienceQuestionnaire": 
                [{
                   "element":"text",
                   "label":"<h3>Welcome!</h3><p>Tell some info about yourself before entering chat</p>",
                   "name":"intro"
                },
                {
                   "element":"text",
                   "label":"Hello! How can we help?",
                   "name":"intro"
                },
                {
                    "element":"radio",
                    "name":"Aiheet",
                    "label":"Hei. Voin auttaa sinua koronavirusta (COVID-19) koskevissa kysymyksissä. Mitä tietoa etsit?",
                    "required":true,
                    "options":[
                        {
                        "label":"Mikä on koronavirus?",
                        "value":"Mikä on koronavirus"
                        },
                    ]
            }],
            "postAudienceQuestionnaire":
                [{
                   "element":"text",
                   "label":"<h3>Welcome!</h3><p>Tell some info about yourself before entering chat</p>",
                   "name":"intro"
                },
                {
                   "element":"text",
                   "label":"Hello! How can we help?",
                   "name":"intro"
                },
                {
                    "element":"radio",
                    "name":"Aiheet",
                    "label":"Hei. Voin auttaa sinua koronavirusta (COVID-19) koskevissa kysymyksissä. Mitä tietoa etsit?",
                    "required":true,
                    "options":[
                        {
                        "label":"Mikä on koronavirus?",
                        "value":"Mikä on koronavirus"
                        },
                    ]
            }],
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

    @Test
    fun `should_return_true_for_requireOpenQuestionnaireActivity_with_pre-audience_questionnaire_and_when_session_is_not_resumed`() {
        val currentQueue = NinchatQueue(id = "1234", name = "test-q1")
        val ninchatQueueListPresenter = NinchatQueueListPresenter(listOf(currentQueue))

        NinchatSession.Builder(appContext, configurationKey).create()
        NinchatSessionManager.getInstance().ninchatState.siteConfig.setConfigString(siteConfig)

        Assert.assertEquals(true, ninchatQueueListPresenter.requireOpenQuestionnaireActivity())
    }
}