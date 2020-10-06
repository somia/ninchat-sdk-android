package com.ninchat.sdk.espresso.ninchatreview.model

import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaireHolder
import com.ninchat.sdk.ninchatreview.model.NinchatReviewModel
import org.junit.Assert
import org.junit.Test

class NinchatReviewModelTest {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val configurationKey = appContext.getString(com.ninchat.sdk.R.string.ninchat_configuration_key)

    val siteConfig = """
        {
          "description": "SDK test queues",
          "default": {
            "questionnaireName": "Botti",
            "questionnaireAvatar": "http://testquestion.avatar.img",
            "postAudienceQuestionnaireStyle": "conversation",
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
    fun `should_parse_bot_name_and_bot_avater`() {
        NinchatSession.Builder(appContext, configurationKey).create()
        NinchatSessionManager.getInstance().ninchatState.siteConfig.setConfigString(siteConfig)
        NinchatSessionManager.getInstance().ninchatState.ninchatQuestionnaire = NinchatQuestionnaireHolder(NinchatSessionManager.getInstance())

        val ninchatReviewModel = NinchatReviewModel()
        Assert.assertEquals("Botti", ninchatReviewModel.getBotName())
        Assert.assertEquals("http://testquestion.avatar.img", ninchatReviewModel.getBotAvatar())
    }

    @Test
    fun `conversation_like_questionnaire_should_return_false_as_default`() {
        NinchatSession.Builder(appContext, configurationKey).create()
        val ninchatReviewModel = NinchatReviewModel()
        Assert.assertEquals(false, ninchatReviewModel.isConversationLikeQuestionnaire())

    }

    @Test
    fun `conversation_like_questionnaire_should_return_true`() {
        NinchatSession.Builder(appContext, configurationKey).create()
        NinchatSessionManager.getInstance().ninchatState.siteConfig.setConfigString(siteConfig)
        NinchatSessionManager.getInstance().ninchatState.ninchatQuestionnaire = NinchatQuestionnaireHolder(NinchatSessionManager.getInstance())

        val ninchatReviewModel = NinchatReviewModel()
        Assert.assertEquals(true, ninchatReviewModel.isConversationLikeQuestionnaire())
    }

    @Test
    fun `should_be_able_to_parse_review_related_text_from_site_config`() {
        NinchatSession.Builder(appContext, configurationKey).create()
        NinchatSessionManager.getInstance().ninchatState.siteConfig.setConfigString(siteConfig)
        NinchatSessionManager.getInstance().ninchatState.ninchatQuestionnaire = NinchatQuestionnaireHolder(NinchatSessionManager.getInstance())

        val ninchatReviewModel = NinchatReviewModel()
        Assert.assertEquals("Thank you for the conversation!", ninchatReviewModel.getThanksYouText())
        Assert.assertEquals("<strong>Miten asiointisi onnistui?</strong>", ninchatReviewModel.getFeedbackTitleText())
        Assert.assertEquals("Hyvin", ninchatReviewModel.getFeedbackPositiveText())
        Assert.assertEquals("Ok", ninchatReviewModel.getFeedbackNeutralText())
        Assert.assertEquals("Ohita", ninchatReviewModel.getFeedbackSkipText())
    }
}